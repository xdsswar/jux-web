/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 *
 * - Open-source use: Free (see License for conditions)
 * - Commercial use: Requires explicit written permission from Xtreme Software Solutions
 *
 * This software is provided "AS IS", without warranty of any kind.
 * See the LICENSE file in the project root for full terms.
 */

package xss.it.jux.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Compile-time annotation processor for the JUX framework.
 *
 * <p>This processor runs during the Java compilation phase and performs two major tasks:</p>
 *
 * <ol>
 *   <li><b>{@code @Route} validation</b> -- Ensures that every class annotated with
 *       {@link xss.it.jux.annotation.Route} meets the framework's structural requirements:
 *       <ul>
 *         <li>The annotated class extends {@code xss.it.jux.core.Component}.</li>
 *         <li>The route path pattern is syntactically valid (starts with {@code /}, no double
 *             slashes, properly formed variable expressions, supported type hints).</li>
 *         <li>HTTP methods are specified (they always are via the annotation default, but this
 *             validates the array is non-empty).</li>
 *         <li>{@code @PathParam} fields have names that correspond to variables declared in
 *             the route pattern.</li>
 *         <li>{@code @PathParam} field types are among the supported JUX type coercion targets.</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code @MessageBundle} validation and code generation</b> -- Ensures that every
 *       interface annotated with {@link xss.it.jux.annotation.MessageBundle} is structurally
 *       correct, and generates concrete implementation classes:
 *       <ul>
 *         <li>The annotated element must be an interface (not a class, enum, or record).</li>
 *         <li>All declared methods must return {@code java.lang.String}.</li>
 *         <li>All methods must be annotated with {@code @Message}.</li>
 *         <li>The {@code @Message} value must be a valid {@link java.text.MessageFormat} pattern.</li>
 *         <li>A concrete implementation class is generated that uses {@code MessageFormat} to
 *             format the message strings with the method parameters.</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h3>Error reporting</h3>
 * <p>All validation errors are reported through
 * {@link Messager#printMessage(Diagnostic.Kind, CharSequence, Element)} with
 * {@link Diagnostic.Kind#ERROR}, which causes the compilation to fail. Warnings use
 * {@link Diagnostic.Kind#WARNING}.</p>
 *
 * <h3>Registration</h3>
 * <p>This processor is registered via the standard Java SPI mechanism in
 * {@code META-INF/services/javax.annotation.processing.Processor}.</p>
 *
 * @see RouteValidator
 * @see xss.it.jux.annotation.Route
 * @see xss.it.jux.annotation.MessageBundle
 * @see xss.it.jux.annotation.Message
 */
@SupportedAnnotationTypes({
        "xss.it.jux.annotation.Route",
        "xss.it.jux.annotation.MessageBundle"
})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class JuxAnnotationProcessor extends AbstractProcessor {

    // ── Fully-qualified annotation names ──────────────────────────────────────
    // These constants avoid string literals scattered throughout the processor and ensure
    // consistent references when looking up annotation mirrors and type elements.

    /** Fully-qualified name of the {@code @Route} annotation. */
    private static final String ROUTE_ANNOTATION = "xss.it.jux.annotation.Route";

    /** Fully-qualified name of the {@code @MessageBundle} annotation. */
    private static final String MESSAGE_BUNDLE_ANNOTATION = "xss.it.jux.annotation.MessageBundle";

    /** Fully-qualified name of the {@code @Message} annotation. */
    private static final String MESSAGE_ANNOTATION = "xss.it.jux.annotation.Message";

    /** Fully-qualified name of the {@code @PathParam} annotation. */
    private static final String PATH_PARAM_ANNOTATION = "xss.it.jux.annotation.PathParam";

    /** Fully-qualified name of the {@code @MessageLocale} annotation. */
    private static final String MESSAGE_LOCALE_ANNOTATION = "xss.it.jux.annotation.MessageLocale";

    /** Fully-qualified name of the {@code Component} base class. */
    private static final String COMPONENT_CLASS = "xss.it.jux.core.Component";

    // ── Processing environment utilities ──────────────────────────────────────
    // These are initialized once in init() and used throughout all rounds.

    /** Compiler messager for reporting errors and warnings. */
    private Messager messager;

    /** Element utilities for resolving type names and packages. */
    private Elements elementUtils;

    /** Type utilities for subtype checks and type comparisons. */
    private Types typeUtils;

    /** Filer for generating source files during annotation processing. */
    private Filer filer;

    /**
     * Initializes the processor with the compilation environment.
     *
     * <p>This method is called once by the Java compiler before any processing rounds
     * begin. It caches references to the {@link Messager}, {@link Elements},
     * {@link Types}, and {@link Filer} utilities provided by the processing
     * environment.</p>
     *
     * @param processingEnv the processing environment provided by the compiler
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
    }

    /**
     * Processes annotations in a single round of annotation processing.
     *
     * <p>The Java compiler calls this method once per round. In each round, the processor
     * receives the set of annotation type elements being processed and the round
     * environment containing all elements annotated with those annotations.</p>
     *
     * <p>This implementation:</p>
     * <ol>
     *   <li>Processes all {@code @Route}-annotated elements via {@link #processRoutes}.</li>
     *   <li>Processes all {@code @MessageBundle}-annotated elements via
     *       {@link #processMessageBundles}.</li>
     * </ol>
     *
     * <p>Returns {@code false} to allow other processors to also handle these annotations
     * if needed (cooperative processing).</p>
     *
     * @param annotations the set of annotation types requested for processing in this round
     * @param roundEnv    the round environment containing elements annotated with the
     *                    requested annotation types
     * @return {@code false} always, to allow other processors to claim these annotations
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Skip processing during the final round (no new sources to process).
        if (roundEnv.processingOver()) {
            return false;
        }

        // Process @Route annotations.
        processRoutes(roundEnv);

        // Process @MessageBundle annotations.
        processMessageBundles(roundEnv);

        // Return false: don't claim the annotations exclusively. Other processors (e.g.
        // Spring's own processors) may also need to see @Route-annotated classes.
        return false;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  @Route Processing
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * Processes all elements annotated with {@code @Route}.
     *
     * <p>For each annotated class, this method:</p>
     * <ol>
     *   <li>Verifies the annotated element is a class (not an interface, enum, etc.).</li>
     *   <li>Verifies the class extends {@code xss.it.jux.core.Component}.</li>
     *   <li>Extracts the route path from the annotation and validates it using
     *       {@link RouteValidator#validatePath}.</li>
     *   <li>Validates that at least one HTTP method is specified.</li>
     *   <li>Collects all {@code @PathParam}-annotated fields and validates that they
     *       match the route pattern variables and use supported types.</li>
     * </ol>
     *
     * @param roundEnv the current round environment
     */
    private void processRoutes(RoundEnvironment roundEnv) {
        // Look up the @Route TypeElement. If it's not on the classpath (shouldn't happen
        // because jux-annotations is a compile dependency), skip silently.
        TypeElement routeAnnotationType = elementUtils.getTypeElement(ROUTE_ANNOTATION);
        if (routeAnnotationType == null) {
            return;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(routeAnnotationType)) {
            // @Route should only appear on classes. If it's on something else, report an error.
            if (element.getKind() != ElementKind.CLASS) {
                error("@Route can only be applied to classes, but found on "
                        + element.getKind().name().toLowerCase() + " '" + element.getSimpleName()
                        + "'.", element);
                continue;
            }

            TypeElement classElement = (TypeElement) element;

            // -- Check 1: Must extend Component --
            validateExtendsComponent(classElement);

            // -- Check 2: Validate the route path --
            String routePath = extractRoutePath(classElement);
            if (routePath != null) {
                validateRoutePath(routePath, classElement);

                // -- Check 3: Validate @PathParam fields match route variables --
                validatePathParams(routePath, classElement);
            }

            // -- Check 4: Validate HTTP methods are specified --
            validateHttpMethods(classElement);
        }
    }

    /**
     * Verifies that the given class element extends {@code xss.it.jux.core.Component}.
     *
     * <p>This check walks the type hierarchy of the annotated class using
     * {@link Types#isSubtype} to determine whether {@code Component} is an ancestor.
     * If the {@code Component} class cannot be resolved (e.g. jux-core is not on the
     * classpath), a warning is emitted instead of an error, since the check cannot be
     * performed reliably.</p>
     *
     * @param classElement the class annotated with {@code @Route}
     */
    private void validateExtendsComponent(TypeElement classElement) {
        // Resolve the Component TypeElement from the classpath.
        TypeElement componentType = elementUtils.getTypeElement(COMPONENT_CLASS);
        if (componentType == null) {
            // Component class is not on the annotation processor's classpath. This is
            // unexpected but not fatal -- the compiler will catch the missing class later.
            warning("Cannot resolve " + COMPONENT_CLASS + " to validate @Route class '"
                    + classElement.getQualifiedName() + "'. Ensure jux-core is on the classpath.",
                    classElement);
            return;
        }

        TypeMirror componentMirror = componentType.asType();
        TypeMirror classMirror = classElement.asType();

        // Use Types.isSubtype for a thorough hierarchy check. This returns true if
        // classMirror IS componentMirror or any of its supertypes match.
        if (!typeUtils.isSubtype(typeUtils.erasure(classMirror), typeUtils.erasure(componentMirror))) {
            error("@Route class '" + classElement.getQualifiedName()
                    + "' must extend " + COMPONENT_CLASS + ".", classElement);
        }
    }

    /**
     * Extracts the route path string from the {@code @Route} annotation on a class.
     *
     * <p>Uses the annotation mirror API to read the {@code value()} attribute of the
     * {@code @Route} annotation. Returns {@code null} if the annotation or its value
     * cannot be read (should not happen for a correctly compiled annotation).</p>
     *
     * @param classElement the class annotated with {@code @Route}
     * @return the route path string, or {@code null} if it cannot be determined
     */
    private String extractRoutePath(TypeElement classElement) {
        // Walk the annotation mirrors on the class to find @Route.
        for (var annotationMirror : classElement.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            if (ROUTE_ANNOTATION.equals(annotationName)) {
                // Extract the "value" attribute from the annotation mirror.
                for (var entry : annotationMirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        return entry.getValue().getValue().toString();
                    }
                }
            }
        }

        // The @Route annotation should always have a value() since it has no default.
        // If we reach here, something unexpected happened.
        warning("Could not extract route path from @Route on '"
                + classElement.getQualifiedName() + "'.", classElement);
        return null;
    }

    /**
     * Validates the extracted route path using {@link RouteValidator#validatePath}.
     *
     * <p>Each validation error from the {@code RouteValidator} is reported as a
     * compilation error attached to the class element.</p>
     *
     * @param routePath    the route path string to validate
     * @param classElement the class element (for error reporting location)
     */
    private void validateRoutePath(String routePath, TypeElement classElement) {
        List<String> errors = RouteValidator.validatePath(routePath);
        for (String validationError : errors) {
            error("Invalid route path on '" + classElement.getSimpleName() + "': "
                    + validationError, classElement);
        }
    }

    /**
     * Validates that the {@code @Route} annotation specifies at least one HTTP method.
     *
     * <p>The {@code @Route} annotation has a default of {@code {HttpMethod.GET}}, so this
     * check primarily catches the case where someone explicitly sets an empty array:
     * {@code @Route(value = "/x", methods = {})}.</p>
     *
     * @param classElement the class annotated with {@code @Route}
     */
    private void validateHttpMethods(TypeElement classElement) {
        // Walk annotation mirrors to find the "methods" attribute.
        for (var annotationMirror : classElement.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            if (ROUTE_ANNOTATION.equals(annotationName)) {
                for (var entry : annotationMirror.getElementValues().entrySet()) {
                    if ("methods".equals(entry.getKey().getSimpleName().toString())) {
                        // The value is an AnnotationValue wrapping a List of AnnotationValues.
                        Object value = entry.getValue().getValue();
                        if (value instanceof java.util.List<?> list && list.isEmpty()) {
                            error("@Route on '" + classElement.getSimpleName()
                                    + "' must specify at least one HTTP method.", classElement);
                        }
                        return;
                    }
                }
                // If "methods" is not explicitly set, the default {GET} applies -- valid.
            }
        }
    }

    /**
     * Collects {@code @PathParam}-annotated fields from the route class and validates
     * that their names match route pattern variables and their types are supported.
     *
     * <p>This method iterates over all enclosed elements of the class looking for fields
     * annotated with {@code @PathParam}. For each such field:</p>
     * <ul>
     *   <li>The param name is resolved (from {@code @PathParam.value()} or the field name).</li>
     *   <li>The param name is checked against the route pattern's variable names.</li>
     *   <li>The field type is checked against the set of supported JUX coercion types.</li>
     * </ul>
     *
     * @param routePath    the validated route path pattern
     * @param classElement the class containing the fields
     */
    private void validatePathParams(String routePath, TypeElement classElement) {
        // Resolve the @PathParam annotation type.
        TypeElement pathParamType = elementUtils.getTypeElement(PATH_PARAM_ANNOTATION);
        if (pathParamType == null) {
            // @PathParam not on classpath -- skip validation.
            return;
        }

        // Extract variable names from the route pattern for cross-referencing.
        Set<String> routeVariables = RouteValidator.extractVariableNames(routePath);

        // Collect all @PathParam field names for the bulk match check.
        Set<String> declaredParamNames = new LinkedHashSet<>();

        for (Element enclosed : classElement.getEnclosedElements()) {
            // Only process fields (not methods, constructors, etc.).
            if (enclosed.getKind() != ElementKind.FIELD) {
                continue;
            }

            VariableElement field = (VariableElement) enclosed;

            // Check if this field has a @PathParam annotation.
            String paramName = extractPathParamName(field);
            if (paramName == null) {
                // Field is not annotated with @PathParam -- skip.
                continue;
            }

            declaredParamNames.add(paramName);

            // Validate that the param name corresponds to a route variable.
            if (!routeVariables.contains(paramName)) {
                error("@PathParam(\"" + paramName + "\") on field '"
                        + field.getSimpleName() + "' in '" + classElement.getSimpleName()
                        + "' does not match any variable in route pattern \""
                        + routePath + "\".", field);
            }

            // Validate the field type is a supported coercion target.
            validatePathParamFieldType(field, classElement);
        }
    }

    /**
     * Extracts the effective param name from a {@code @PathParam}-annotated field.
     *
     * <p>If {@code @PathParam.value()} is non-empty, that value is used. Otherwise,
     * the field's own name is used as the param name.</p>
     *
     * @param field the field element to inspect
     * @return the effective param name, or {@code null} if the field is not annotated
     *         with {@code @PathParam}
     */
    private String extractPathParamName(VariableElement field) {
        for (var annotationMirror : field.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            if (PATH_PARAM_ANNOTATION.equals(annotationName)) {
                // Look for the "value" attribute.
                for (var entry : annotationMirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        String value = entry.getValue().getValue().toString();
                        if (!value.isEmpty()) {
                            return value;
                        }
                    }
                }
                // value() was empty or not set -- use the field name.
                return field.getSimpleName().toString();
            }
        }
        // No @PathParam annotation found on this field.
        return null;
    }

    /**
     * Validates that a {@code @PathParam} field's type is one of the supported JUX
     * type coercion targets.
     *
     * <p>Supported types are: {@code String}, {@code long}/{@code Long},
     * {@code int}/{@code Integer}, {@code double}/{@code Double},
     * {@code boolean}/{@code Boolean}, {@code UUID}, {@code LocalDate}, and any
     * enum type.</p>
     *
     * @param field        the {@code @PathParam}-annotated field
     * @param classElement the enclosing class (for context in error messages)
     */
    private void validatePathParamFieldType(VariableElement field, TypeElement classElement) {
        TypeMirror fieldType = field.asType();

        // For primitive types, check the TypeKind directly.
        if (fieldType.getKind().isPrimitive()) {
            boolean supported = switch (fieldType.getKind()) {
                case LONG, INT, DOUBLE, BOOLEAN -> true;
                default -> false;
            };
            if (!supported) {
                error("@PathParam field '" + field.getSimpleName() + "' in '"
                        + classElement.getSimpleName() + "' has unsupported primitive type '"
                        + fieldType.getKind().name().toLowerCase()
                        + "'. Supported primitives: long, int, double, boolean.", field);
            }
            return;
        }

        // For declared (reference) types, get the fully-qualified name and check it.
        String qualifiedName = fieldType.toString();

        // Check if it's an enum type by looking at the type element's kind.
        if (isEnumType(fieldType)) {
            // Enum types are always supported.
            return;
        }

        if (!RouteValidator.isSupportedParamType(qualifiedName)) {
            error("@PathParam field '" + field.getSimpleName() + "' in '"
                    + classElement.getSimpleName() + "' has unsupported type '"
                    + qualifiedName + "'. Supported types: String, long, int, double, "
                    + "boolean, UUID, LocalDate, and Enum subtypes.", field);
        }
    }

    /**
     * Determines whether a {@link TypeMirror} represents an enum type.
     *
     * <p>This is done by resolving the type mirror to a {@link TypeElement} and checking
     * its element kind. This approach is more reliable than string matching because it
     * handles parameterized types and type aliases correctly.</p>
     *
     * @param typeMirror the type to check
     * @return {@code true} if the type is an enum, {@code false} otherwise
     */
    private boolean isEnumType(TypeMirror typeMirror) {
        // Only declared types can be enums.
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }

        // Resolve to the type element and check its kind.
        Element typeElement = typeUtils.asElement(typeMirror);
        return typeElement != null && typeElement.getKind() == ElementKind.ENUM;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  @MessageBundle Processing
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * Processes all elements annotated with {@code @MessageBundle}.
     *
     * <p>For each annotated interface, this method:</p>
     * <ol>
     *   <li>Verifies the annotated element is an interface.</li>
     *   <li>Validates that all declared methods return {@code String}.</li>
     *   <li>Validates that all methods have a {@code @Message} annotation.</li>
     *   <li>Validates that each {@code @Message} pattern is a syntactically valid
     *       {@link java.text.MessageFormat} pattern.</li>
     *   <li>Generates a concrete implementation class for the default locale.</li>
     * </ol>
     *
     * @param roundEnv the current round environment
     */
    private void processMessageBundles(RoundEnvironment roundEnv) {
        // Look up the @MessageBundle TypeElement.
        TypeElement bundleAnnotationType = elementUtils.getTypeElement(MESSAGE_BUNDLE_ANNOTATION);
        if (bundleAnnotationType == null) {
            return;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(bundleAnnotationType)) {
            // -- Check 1: Must be an interface --
            if (element.getKind() != ElementKind.INTERFACE) {
                error("@MessageBundle can only be applied to interfaces, but found on "
                        + element.getKind().name().toLowerCase() + " '"
                        + element.getSimpleName() + "'.", element);
                continue;
            }

            TypeElement interfaceElement = (TypeElement) element;

            // Skip locale-specific sub-interfaces (those have @MessageLocale). They extend
            // the base bundle and override messages; we only generate for the base.
            if (hasAnnotation(interfaceElement, MESSAGE_LOCALE_ANNOTATION)) {
                // Locale sub-interfaces are validated but not generated here -- the base
                // bundle generation handles them indirectly.
                validateMessageBundleMethods(interfaceElement);
                continue;
            }

            // -- Check 2 & 3: Validate method signatures and @Message presence --
            validateMessageBundleMethods(interfaceElement);

            // -- Check 4: Generate implementation class --
            generateMessageBundleImpl(interfaceElement);
        }
    }

    /**
     * Validates all declared methods of a {@code @MessageBundle} interface.
     *
     * <p>For each method declared directly on the interface (not inherited), this
     * method checks:</p>
     * <ul>
     *   <li>The return type is {@code java.lang.String}.</li>
     *   <li>The method is annotated with {@code @Message}.</li>
     *   <li>The {@code @Message} pattern is a valid {@link java.text.MessageFormat} string.</li>
     * </ul>
     *
     * @param interfaceElement the message bundle interface to validate
     */
    private void validateMessageBundleMethods(TypeElement interfaceElement) {
        // Resolve the String type for return-type comparison.
        TypeMirror stringType = elementUtils.getTypeElement("java.lang.String").asType();

        for (Element enclosed : interfaceElement.getEnclosedElements()) {
            // Only process methods (interfaces can also contain constants, nested types, etc.).
            if (enclosed.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement method = (ExecutableElement) enclosed;

            // -- Return type must be String --
            if (!typeUtils.isSameType(method.getReturnType(), stringType)) {
                error("@MessageBundle method '" + method.getSimpleName() + "' in '"
                        + interfaceElement.getSimpleName() + "' must return String, but returns '"
                        + method.getReturnType() + "'.", method);
            }

            // -- Must have @Message annotation --
            String messagePattern = extractMessagePattern(method);
            if (messagePattern == null) {
                error("@MessageBundle method '" + method.getSimpleName() + "' in '"
                        + interfaceElement.getSimpleName()
                        + "' is missing a @Message annotation.", method);
                continue;
            }

            // -- Validate the MessageFormat pattern syntax --
            validateMessageFormatPattern(messagePattern, method, interfaceElement);
        }
    }

    /**
     * Extracts the {@code @Message} pattern string from an executable element.
     *
     * <p>Searches the annotation mirrors on the method for a {@code @Message} annotation
     * and returns its {@code value()} attribute. Returns {@code null} if no
     * {@code @Message} is present.</p>
     *
     * @param method the method to inspect
     * @return the message pattern string, or {@code null} if not annotated
     */
    private String extractMessagePattern(ExecutableElement method) {
        for (var annotationMirror : method.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            if (MESSAGE_ANNOTATION.equals(annotationName)) {
                for (var entry : annotationMirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        return entry.getValue().getValue().toString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Validates that a {@code @Message} pattern is a syntactically valid
     * {@link java.text.MessageFormat} string.
     *
     * <p>This is done by attempting to construct a {@code MessageFormat} instance with the
     * pattern. If the constructor throws {@link IllegalArgumentException}, the pattern
     * is invalid and a compilation error is reported.</p>
     *
     * @param pattern          the MessageFormat pattern to validate
     * @param method           the method carrying the {@code @Message} annotation (for error
     *                         reporting location)
     * @param interfaceElement the enclosing interface (for context in error messages)
     */
    private void validateMessageFormatPattern(String pattern, ExecutableElement method,
                                               TypeElement interfaceElement) {
        try {
            // Attempt to parse the pattern. This validates syntax: balanced braces,
            // valid format types (choice, number, date, etc.), and argument indices.
            new MessageFormat(pattern);
        } catch (IllegalArgumentException e) {
            error("Invalid MessageFormat pattern on method '" + method.getSimpleName()
                    + "' in '" + interfaceElement.getSimpleName() + "': \""
                    + pattern + "\". Error: " + e.getMessage(), method);
        }
    }

    /**
     * Generates a concrete implementation class for a {@code @MessageBundle} interface.
     *
     * <p>The generated class:</p>
     * <ul>
     *   <li>Is placed in the same package as the source interface.</li>
     *   <li>Is named {@code <InterfaceName>Impl} (e.g. {@code HomeMessagesImpl}).</li>
     *   <li>Implements the bundle interface.</li>
     *   <li>Provides an implementation for each method that uses {@link java.text.MessageFormat}
     *       to format the {@code @Message} pattern with the method's parameters.</li>
     * </ul>
     *
     * <p>This is a structural implementation -- the generated code is straightforward and
     * does not optimize for advanced scenarios like caching compiled {@code MessageFormat}
     * instances. A production-grade implementation would pre-compile patterns in the
     * constructor or a static initializer.</p>
     *
     * @param interfaceElement the message bundle interface to generate an implementation for
     */
    private void generateMessageBundleImpl(TypeElement interfaceElement) {
        // Determine the package and class names for the generated implementation.
        String packageName = elementUtils.getPackageOf(interfaceElement).getQualifiedName().toString();
        String interfaceName = interfaceElement.getSimpleName().toString();
        String implClassName = interfaceName + "Impl";
        String qualifiedImplName = packageName.isEmpty()
                ? implClassName
                : packageName + "." + implClassName;

        try {
            // Create the source file via the Filer. The originating element is provided
            // so that incremental compilation tools can track the dependency.
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedImplName, interfaceElement);

            try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
                // Write the package declaration.
                if (!packageName.isEmpty()) {
                    writer.println("package " + packageName + ";");
                    writer.println();
                }

                // Write the import for MessageFormat.
                writer.println("import java.text.MessageFormat;");
                writer.println();

                // Write the class Javadoc.
                writer.println("/**");
                writer.println(" * Auto-generated implementation of {@link " + interfaceName + "}.");
                writer.println(" *");
                writer.println(" * <p>Generated by {@code JuxAnnotationProcessor} at compile time.");
                writer.println(" * Do not edit manually -- changes will be overwritten on next build.</p>");
                writer.println(" *");
                writer.println(" * <p>Each method formats its {@code @Message} pattern using");
                writer.println(" * {@link java.text.MessageFormat} with the method parameters.</p>");
                writer.println(" */");
                writer.println("public class " + implClassName + " implements " + interfaceName + " {");
                writer.println();

                // Generate each method implementation.
                for (Element enclosed : interfaceElement.getEnclosedElements()) {
                    if (enclosed.getKind() != ElementKind.METHOD) {
                        continue;
                    }

                    ExecutableElement method = (ExecutableElement) enclosed;
                    String messagePattern = extractMessagePattern(method);

                    // If there is no @Message, skip -- the validation pass already reported it.
                    if (messagePattern == null) {
                        continue;
                    }

                    generateMethodImpl(writer, method, messagePattern);
                }

                // Close the class.
                writer.println("}");
            }

            // Log a note so developers can see that code generation occurred.
            note("Generated message bundle implementation: " + qualifiedImplName);

        } catch (IOException e) {
            error("Failed to generate implementation for @MessageBundle '"
                    + interfaceElement.getQualifiedName() + "': " + e.getMessage(),
                    interfaceElement);
        }
    }

    /**
     * Generates the implementation of a single message bundle method.
     *
     * <p>The generated method uses {@link java.text.MessageFormat#format(String, Object...)}
     * to format the pattern with the method's parameters. If the method has no parameters,
     * the pattern is returned as-is (after single-quote escaping for MessageFormat).</p>
     *
     * <p>Example generated code for {@code @Message("Hello, {0}") String greeting(String name)}:</p>
     * <pre>{@code
     *     @Override
     *     public String greeting(String name) {
     *         return java.text.MessageFormat.format("Hello, {0}", name);
     *     }
     * }</pre>
     *
     * @param writer         the output writer for the generated source
     * @param method         the interface method to implement
     * @param messagePattern the {@code @Message} pattern string
     */
    private void generateMethodImpl(PrintWriter writer, ExecutableElement method,
                                     String messagePattern) {
        List<? extends VariableElement> params = method.getParameters();

        // Build the method signature.
        StringBuilder signature = new StringBuilder();
        signature.append("    /** {@inheritDoc} */\n");
        signature.append("    @Override\n");
        signature.append("    public String ").append(method.getSimpleName()).append("(");

        // Append parameter declarations.
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                signature.append(", ");
            }
            VariableElement param = params.get(i);
            signature.append(param.asType()).append(" ").append(param.getSimpleName());
        }
        signature.append(") {");

        writer.println(signature);

        // Escape the message pattern for Java string literal embedding.
        // Double-escape backslashes and escape double quotes.
        String escapedPattern = messagePattern
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        if (params.isEmpty()) {
            // No parameters -- return the pattern directly. For simple strings without
            // MessageFormat tokens this is straightforward. For patterns that happen to
            // contain literal braces, MessageFormat.format would still work but is
            // unnecessary overhead when there are no arguments.
            writer.println("        return \"" + escapedPattern + "\";");
        } else {
            // Build the MessageFormat.format(...) call with all parameters as arguments.
            StringBuilder formatCall = new StringBuilder();
            formatCall.append("        return MessageFormat.format(\"")
                    .append(escapedPattern)
                    .append("\"");

            for (VariableElement param : params) {
                formatCall.append(", ");
                // Box primitives by casting to Object. MessageFormat.format takes Object[].
                TypeMirror paramType = param.asType();
                if (paramType.getKind().isPrimitive()) {
                    // Autoboxing handles this in modern Java, but being explicit is clearer.
                    formatCall.append("(Object) ").append(param.getSimpleName());
                } else {
                    formatCall.append(param.getSimpleName());
                }
            }

            formatCall.append(");");
            writer.println(formatCall);
        }

        writer.println("    }");
        writer.println();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  Utility Methods
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * Checks whether a type element has a specific annotation by fully-qualified name.
     *
     * <p>This is a helper to detect the presence of annotations like {@code @MessageLocale}
     * without directly depending on the annotation class (which may not be resolvable
     * in all processing environments).</p>
     *
     * @param element             the element to check
     * @param annotationQualified the fully-qualified name of the annotation to look for
     * @return {@code true} if the element carries the specified annotation
     */
    private boolean hasAnnotation(TypeElement element, String annotationQualified) {
        for (var mirror : element.getAnnotationMirrors()) {
            if (annotationQualified.equals(mirror.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reports a compilation error attached to the given element.
     *
     * <p>An error causes the compilation to fail after annotation processing completes.
     * The error message and element location are displayed to the developer in their
     * IDE or build output.</p>
     *
     * @param message the human-readable error message
     * @param element the element that caused the error (used for source location)
     */
    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * Reports a compilation warning attached to the given element.
     *
     * <p>A warning does not cause compilation failure but is displayed to the developer
     * as a diagnostic. Use for non-critical issues that should be addressed but don't
     * prevent correct operation.</p>
     *
     * @param message the human-readable warning message
     * @param element the element that triggered the warning (used for source location)
     */
    private void warning(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    /**
     * Reports an informational note during annotation processing.
     *
     * <p>Notes are low-priority diagnostics used to communicate non-critical information
     * to the developer, such as code generation progress. They may be suppressed by
     * build tool configuration.</p>
     *
     * @param message the informational message
     */
    private void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
