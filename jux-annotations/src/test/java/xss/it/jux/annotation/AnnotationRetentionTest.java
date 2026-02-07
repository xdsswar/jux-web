package xss.it.jux.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies framework-level guarantees about all JUX annotations.
 *
 * <p>These tests ensure that every annotation in the {@code xss.it.jux.annotation}
 * package has the correct retention policy, target element types, repeatable
 * container configuration, and that supporting enums have the expected values.</p>
 */
@DisplayName("JUX Annotation Retention and Metadata")
class AnnotationRetentionTest {

    // ── Test 1: All annotations have RUNTIME retention ──────────────────────

    @Nested
    @DisplayName("RUNTIME Retention")
    class RuntimeRetention {

        static Stream<Class<?>> allAnnotations() {
            return Stream.of(
                    Route.class,
                    PathParam.class,
                    QueryParam.class,
                    HeaderParam.class,
                    CookieParam.class,
                    SessionParam.class,
                    RequestContext.class,
                    LocaleParam.class,
                    Css.class,
                    Js.class,
                    Title.class,
                    Meta.class,
                    Favicon.class,
                    Canonical.class,
                    Layout.class,
                    ServerMeta.class,
                    InlineCss.class,
                    InlineJs.class,
                    JuxComponent.class,
                    Prop.class,
                    On.class,
                    State.class,
                    OnMount.class,
                    OnUnmount.class,
                    MessageBundle.class,
                    MessageLocale.class,
                    Message.class,
                    Localized.class
            );
        }

        @ParameterizedTest(name = "@{0} has RUNTIME retention")
        @MethodSource("allAnnotations")
        void annotationHasRuntimeRetention(Class<?> annotationClass) {
            Retention retention = annotationClass.getAnnotation(Retention.class);

            assertThat(retention)
                    .as("@%s must have @Retention", annotationClass.getSimpleName())
                    .isNotNull();

            assertThat(retention.value())
                    .as("@%s must have RUNTIME retention", annotationClass.getSimpleName())
                    .isEqualTo(RetentionPolicy.RUNTIME);
        }
    }

    // ── Test 2: Repeatable annotations have container annotations ───────────

    @Nested
    @DisplayName("Repeatable Container Annotations")
    class RepeatableContainers {

        @Test
        @DisplayName("@Css is @Repeatable with Css.List container")
        void cssIsRepeatable() {
            assertRepeatableWithContainer(Css.class, Css.List.class);
        }

        @Test
        @DisplayName("@Js is @Repeatable with Js.List container")
        void jsIsRepeatable() {
            assertRepeatableWithContainer(Js.class, Js.List.class);
        }

        @Test
        @DisplayName("@Meta is @Repeatable with Meta.List container")
        void metaIsRepeatable() {
            assertRepeatableWithContainer(Meta.class, Meta.List.class);
        }

        @Test
        @DisplayName("@ServerMeta is @Repeatable with ServerMeta.List container")
        void serverMetaIsRepeatable() {
            assertRepeatableWithContainer(ServerMeta.class, ServerMeta.List.class);
        }

        @Test
        @DisplayName("Css.List container has RUNTIME retention")
        void cssListHasRuntimeRetention() {
            assertContainerHasRuntimeRetention(Css.List.class);
        }

        @Test
        @DisplayName("Js.List container has RUNTIME retention")
        void jsListHasRuntimeRetention() {
            assertContainerHasRuntimeRetention(Js.List.class);
        }

        @Test
        @DisplayName("Meta.List container has RUNTIME retention")
        void metaListHasRuntimeRetention() {
            assertContainerHasRuntimeRetention(Meta.List.class);
        }

        @Test
        @DisplayName("ServerMeta.List container has RUNTIME retention")
        void serverMetaListHasRuntimeRetention() {
            assertContainerHasRuntimeRetention(ServerMeta.List.class);
        }

        @Test
        @DisplayName("Css.List container has a value() method returning Css[]")
        void cssListContainerHasValueMethod() {
            assertContainerHasValueMethod(Css.List.class, Css[].class);
        }

        @Test
        @DisplayName("Js.List container has a value() method returning Js[]")
        void jsListContainerHasValueMethod() {
            assertContainerHasValueMethod(Js.List.class, Js[].class);
        }

        @Test
        @DisplayName("Meta.List container has a value() method returning Meta[]")
        void metaListContainerHasValueMethod() {
            assertContainerHasValueMethod(Meta.List.class, Meta[].class);
        }

        @Test
        @DisplayName("ServerMeta.List container has a value() method returning ServerMeta[]")
        void serverMetaListContainerHasValueMethod() {
            assertContainerHasValueMethod(ServerMeta.List.class, ServerMeta[].class);
        }

        private void assertRepeatableWithContainer(Class<?> annotation, Class<?> expectedContainer) {
            Repeatable repeatable = annotation.getAnnotation(Repeatable.class);

            assertThat(repeatable)
                    .as("@%s must be annotated with @Repeatable", annotation.getSimpleName())
                    .isNotNull();

            assertThat(repeatable.value())
                    .as("@%s container must be %s",
                            annotation.getSimpleName(), expectedContainer.getSimpleName())
                    .isEqualTo(expectedContainer);
        }

        private void assertContainerHasRuntimeRetention(Class<?> containerClass) {
            Retention retention = containerClass.getAnnotation(Retention.class);

            assertThat(retention)
                    .as("%s must have @Retention", containerClass.getSimpleName())
                    .isNotNull();

            assertThat(retention.value())
                    .as("%s must have RUNTIME retention", containerClass.getSimpleName())
                    .isEqualTo(RetentionPolicy.RUNTIME);
        }

        private void assertContainerHasValueMethod(Class<?> containerClass, Class<?> expectedReturnType) {
            Method valueMethod;
            try {
                valueMethod = containerClass.getDeclaredMethod("value");
            } catch (NoSuchMethodException e) {
                throw new AssertionError(
                        containerClass.getSimpleName() + " must declare a value() method", e);
            }

            assertThat(valueMethod.getReturnType())
                    .as("%s.value() must return %s",
                            containerClass.getSimpleName(), expectedReturnType.getSimpleName())
                    .isEqualTo(expectedReturnType);
        }
    }

    // ── Test 3: Target element types are correct ────────────────────────────

    @Nested
    @DisplayName("Target Element Types")
    class TargetElementTypes {

        @Test
        @DisplayName("@Route targets TYPE")
        void routeTargetsType() {
            assertTarget(Route.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@PathParam targets FIELD")
        void pathParamTargetsField() {
            assertTarget(PathParam.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@QueryParam targets FIELD")
        void queryParamTargetsField() {
            assertTarget(QueryParam.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@HeaderParam targets FIELD")
        void headerParamTargetsField() {
            assertTarget(HeaderParam.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@CookieParam targets FIELD")
        void cookieParamTargetsField() {
            assertTarget(CookieParam.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@SessionParam targets FIELD")
        void sessionParamTargetsField() {
            assertTarget(SessionParam.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@RequestContext targets FIELD")
        void requestContextTargetsField() {
            assertTarget(RequestContext.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@LocaleParam targets FIELD")
        void localeParamTargetsField() {
            assertTarget(LocaleParam.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@Title targets TYPE")
        void titleTargetsType() {
            assertTarget(Title.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Css targets TYPE")
        void cssTargetsType() {
            assertTarget(Css.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Js targets TYPE")
        void jsTargetsType() {
            assertTarget(Js.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Meta targets TYPE")
        void metaTargetsType() {
            assertTarget(Meta.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Favicon targets TYPE")
        void faviconTargetsType() {
            assertTarget(Favicon.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Canonical targets TYPE")
        void canonicalTargetsType() {
            assertTarget(Canonical.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Layout targets TYPE")
        void layoutTargetsType() {
            assertTarget(Layout.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@ServerMeta targets TYPE")
        void serverMetaTargetsType() {
            assertTarget(ServerMeta.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@InlineCss targets METHOD")
        void inlineCssTargetsMethod() {
            assertTarget(InlineCss.class, ElementType.METHOD);
        }

        @Test
        @DisplayName("@InlineJs targets METHOD")
        void inlineJsTargetsMethod() {
            assertTarget(InlineJs.class, ElementType.METHOD);
        }

        @Test
        @DisplayName("@JuxComponent targets TYPE")
        void juxComponentTargetsType() {
            assertTarget(JuxComponent.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Prop targets FIELD")
        void propTargetsField() {
            assertTarget(Prop.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@On targets METHOD")
        void onTargetsMethod() {
            assertTarget(On.class, ElementType.METHOD);
        }

        @Test
        @DisplayName("@State targets FIELD")
        void stateTargetsField() {
            assertTarget(State.class, ElementType.FIELD);
        }

        @Test
        @DisplayName("@OnMount targets METHOD")
        void onMountTargetsMethod() {
            assertTarget(OnMount.class, ElementType.METHOD);
        }

        @Test
        @DisplayName("@OnUnmount targets METHOD")
        void onUnmountTargetsMethod() {
            assertTarget(OnUnmount.class, ElementType.METHOD);
        }

        @Test
        @DisplayName("@MessageBundle targets TYPE")
        void messageBundleTargetsType() {
            assertTarget(MessageBundle.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@MessageLocale targets TYPE")
        void messageLocaleTargetsType() {
            assertTarget(MessageLocale.class, ElementType.TYPE);
        }

        @Test
        @DisplayName("@Message targets METHOD")
        void messageTargetsMethod() {
            assertTarget(Message.class, ElementType.METHOD);
        }

        @Test
        @DisplayName("@Localized targets TYPE")
        void localizedTargetsType() {
            assertTarget(Localized.class, ElementType.TYPE);
        }

        private void assertTarget(Class<?> annotationClass, ElementType expectedTarget) {
            Target target = annotationClass.getAnnotation(Target.class);

            assertThat(target)
                    .as("@%s must have @Target", annotationClass.getSimpleName())
                    .isNotNull();

            assertThat(target.value())
                    .as("@%s must target %s", annotationClass.getSimpleName(), expectedTarget)
                    .contains(expectedTarget);
        }
    }

    // ── Test 4: Enum values exist ───────────────────────────────────────────

    @Nested
    @DisplayName("Enum Values")
    class EnumValues {

        @Test
        @DisplayName("HttpMethod has all standard HTTP methods")
        void httpMethodHasExpectedValues() {
            assertThat(HttpMethod.values())
                    .containsExactly(
                            HttpMethod.GET,
                            HttpMethod.POST,
                            HttpMethod.PUT,
                            HttpMethod.DELETE,
                            HttpMethod.PATCH,
                            HttpMethod.HEAD,
                            HttpMethod.OPTIONS
                    );
        }

        @Test
        @DisplayName("CssPosition has HEAD and BODY_END")
        void cssPositionHasExpectedValues() {
            assertThat(CssPosition.values())
                    .containsExactly(
                            CssPosition.HEAD,
                            CssPosition.BODY_END
                    );
        }

        @Test
        @DisplayName("JsPosition has HEAD and BODY_END")
        void jsPositionHasExpectedValues() {
            assertThat(JsPosition.values())
                    .containsExactly(
                            JsPosition.HEAD,
                            JsPosition.BODY_END
                    );
        }
    }
}
