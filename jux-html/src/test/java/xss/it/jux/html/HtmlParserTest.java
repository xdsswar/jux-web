package xss.it.jux.html;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link HtmlParser} covering element parsing, attribute handling,
 * text content, void elements, HTML entities, comments, DOCTYPE, error
 * reporting, and edge cases.
 */
@DisplayName("HtmlParser")
class HtmlParserTest {

    // ── Basic element parsing ────────────────────────────────────

    @Nested
    @DisplayName("Basic element parsing")
    class BasicElementParsing {

        @Test
        @DisplayName("Single root div element")
        void singleRootDiv_returnsDivElement() {
            Element result = HtmlParser.parse("<div></div>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).isEmpty();
            assertThat(result.getTextContent()).isNull();
        }

        @Test
        @DisplayName("Nested elements preserve hierarchy")
        void nestedElements_preservesParentChildRelationship() {
            Element result = HtmlParser.parse("<div><span></span></div>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("span");
        }

        @Test
        @DisplayName("Text content inside element")
        void textContent_setsTextOnElement() {
            Element result = HtmlParser.parse("<p>Hello</p>");

            assertThat(result.getTag()).isEqualTo("p");
            assertThat(result.getTextContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Deeply nested elements preserve full hierarchy")
        void deeplyNestedElements_preservesFullHierarchy() {
            Element result = HtmlParser.parse(
                    "<div><section><article><p>Deep</p></article></section></div>");

            assertThat(result.getTag()).isEqualTo("div");
            Element section = result.getChildren().getFirst();
            assertThat(section.getTag()).isEqualTo("section");
            Element article = section.getChildren().getFirst();
            assertThat(article.getTag()).isEqualTo("article");
            Element p = article.getChildren().getFirst();
            assertThat(p.getTag()).isEqualTo("p");
            assertThat(p.getTextContent()).isEqualTo("Deep");
        }

        @Test
        @DisplayName("Tag names are lowercased")
        void upperCaseTagName_lowercasedInResult() {
            Element result = HtmlParser.parse("<DIV><SPAN>Hi</SPAN></DIV>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("span");
        }
    }

    // ── Void and self-closing elements ───────────────────────────

    @Nested
    @DisplayName("Void and self-closing elements")
    class VoidAndSelfClosingElements {

        @Test
        @DisplayName("br void element has no children")
        void brVoidElement_parsedWithoutClosingTag() {
            Element result = HtmlParser.parse("<div><br></div>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("br");
            assertThat(result.getChildren().getFirst().getChildren()).isEmpty();
        }

        @Test
        @DisplayName("img void element preserves attributes")
        void imgVoidElement_preservesAttributes() {
            Element result = HtmlParser.parse(
                    "<div><img src=\"pic.png\" alt=\"A picture\"></div>");

            Element img = result.getChildren().getFirst();
            assertThat(img.getTag()).isEqualTo("img");
            assertThat(img.getAttributes()).containsEntry("src", "pic.png");
            assertThat(img.getAttributes()).containsEntry("alt", "A picture");
        }

        @Test
        @DisplayName("hr void element parsed correctly")
        void hrVoidElement_parsedCorrectly() {
            Element result = HtmlParser.parse("<div><hr></div>");

            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("hr");
        }

        @Test
        @DisplayName("input void element parsed correctly")
        void inputVoidElement_parsedCorrectly() {
            Element result = HtmlParser.parse("<div><input type=\"text\"></div>");

            Element input = result.getChildren().getFirst();
            assertThat(input.getTag()).isEqualTo("input");
            assertThat(input.getAttributes()).containsEntry("type", "text");
        }

        @Test
        @DisplayName("XHTML self-closing br/ parsed as void")
        void xhtmlSelfClosingBr_parsedAsVoid() {
            Element result = HtmlParser.parse("<div><br/></div>");

            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("br");
        }

        @Test
        @DisplayName("XHTML self-closing with space br / parsed as void")
        void xhtmlSelfClosingWithSpace_parsedAsVoid() {
            Element result = HtmlParser.parse("<div><br /></div>");

            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("br");
        }
    }

    // ── Attribute parsing ────────────────────────────────────────

    @Nested
    @DisplayName("Attribute parsing")
    class AttributeParsing {

        @Test
        @DisplayName("Double-quoted attribute value")
        void doubleQuotedAttribute_parsedCorrectly() {
            Element result = HtmlParser.parse("<div class=\"foo\"></div>");

            assertThat(result.getCssClasses()).containsExactly("foo");
        }

        @Test
        @DisplayName("Single-quoted attribute value")
        void singleQuotedAttribute_parsedCorrectly() {
            Element result = HtmlParser.parse("<div class='bar'></div>");

            assertThat(result.getCssClasses()).containsExactly("bar");
        }

        @Test
        @DisplayName("Unquoted attribute value")
        void unquotedAttribute_parsedCorrectly() {
            Element result = HtmlParser.parse("<input type=text>");

            assertThat(result.getAttributes()).containsEntry("type", "text");
        }

        @Test
        @DisplayName("Boolean attribute with no value")
        void booleanAttribute_parsedAsEmptyString() {
            Element result = HtmlParser.parse("<input required>");

            assertThat(result.getAttributes()).containsEntry("required", "");
        }

        @Test
        @DisplayName("Multiple attributes on single element")
        void multipleAttributes_allParsed() {
            Element result = HtmlParser.parse(
                    "<input type=\"email\" name=\"user\" placeholder=\"Enter email\">");

            assertThat(result.getAttributes())
                    .containsEntry("type", "email")
                    .containsEntry("name", "user")
                    .containsEntry("placeholder", "Enter email");
        }

        @Test
        @DisplayName("class attribute split into multiple CSS classes")
        void classAttribute_splitIntoMultipleClasses() {
            Element result = HtmlParser.parse("<div class=\"foo bar baz\"></div>");

            assertThat(result.getCssClasses()).containsExactly("foo", "bar", "baz");
        }

        @Test
        @DisplayName("id attribute sets element id")
        void idAttribute_setsElementId() {
            Element result = HtmlParser.parse("<div id=\"main\"></div>");

            assertThat(result.getAttributes()).containsEntry("id", "main");
        }

        @Test
        @DisplayName("style attribute parsed into individual property-value pairs")
        void styleAttribute_parsedIntoPropertyValuePairs() {
            Element result = HtmlParser.parse(
                    "<div style=\"color: red; padding: 1rem\"></div>");

            assertThat(result.getStyles())
                    .containsEntry("color", "red")
                    .containsEntry("padding", "1rem");
        }
    }

    // ── HTML entities ────────────────────────────────────────────

    @Nested
    @DisplayName("HTML entity decoding")
    class HtmlEntityDecoding {

        @Test
        @DisplayName("Named entities decoded: &amp; &lt; &gt; &quot;")
        void namedEntities_decodedCorrectly() {
            Element result = HtmlParser.parse(
                    "<p>&amp; &lt; &gt; &quot;</p>");

            assertThat(result.getTextContent()).isEqualTo("& < > \"");
        }

        @Test
        @DisplayName("Decimal numeric entity &#65; decoded to A")
        void decimalNumericEntity_decodedToCharacter() {
            Element result = HtmlParser.parse("<p>&#65;</p>");

            assertThat(result.getTextContent()).isEqualTo("A");
        }

        @Test
        @DisplayName("Hex numeric entity &#x41; decoded to A")
        void hexNumericEntity_decodedToCharacter() {
            Element result = HtmlParser.parse("<p>&#x41;</p>");

            assertThat(result.getTextContent()).isEqualTo("A");
        }

        @Test
        @DisplayName("&apos; entity decoded to single quote")
        void apostropheEntity_decodedToSingleQuote() {
            Element result = HtmlParser.parse("<p>&apos;</p>");

            assertThat(result.getTextContent()).isEqualTo("'");
        }

        @Test
        @DisplayName("&nbsp; entity decoded to non-breaking space")
        void nbspEntity_decodedToNonBreakingSpace() {
            Element result = HtmlParser.parse("<p>&nbsp;</p>");

            assertThat(result.getTextContent()).isEqualTo("\u00A0");
        }
    }

    // ── Comments and DOCTYPE ─────────────────────────────────────

    @Nested
    @DisplayName("Comments and DOCTYPE handling")
    class CommentsAndDoctype {

        @Test
        @DisplayName("HTML comment skipped")
        void htmlComment_skippedInOutput() {
            Element result = HtmlParser.parse(
                    "<div><!-- this is a comment --><p>Text</p></div>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().getFirst().getTag()).isEqualTo("p");
        }

        @Test
        @DisplayName("DOCTYPE declaration skipped")
        void doctypeDeclaration_skippedInOutput() {
            Element result = HtmlParser.parse(
                    "<!DOCTYPE html><div>Content</div>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getTextContent()).isEqualTo("Content");
        }

        @Test
        @DisplayName("Comment between elements does not break hierarchy")
        void commentBetweenElements_doesNotBreakHierarchy() {
            Element result = HtmlParser.parse(
                    "<div><p>First</p><!-- separator --><p>Second</p></div>");

            assertThat(result.getChildren()).hasSize(2);
            assertThat(result.getChildren().get(0).getTextContent()).isEqualTo("First");
            assertThat(result.getChildren().get(1).getTextContent()).isEqualTo("Second");
        }
    }

    // ── Multiple roots and edge cases ────────────────────────────

    @Nested
    @DisplayName("Multiple roots and edge cases")
    class MultipleRootsAndEdgeCases {

        @Test
        @DisplayName("Multiple root elements wrapped in synthetic div")
        void multipleRoots_wrappedInSyntheticDiv() {
            Element result = HtmlParser.parse("<p>One</p><p>Two</p>");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).hasSize(2);
            assertThat(result.getChildren().get(0).getTag()).isEqualTo("p");
            assertThat(result.getChildren().get(1).getTag()).isEqualTo("p");
        }

        @Test
        @DisplayName("Null HTML returns empty div")
        void nullHtml_returnsEmptyDiv() {
            Element result = HtmlParser.parse(null);

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).isEmpty();
            assertThat(result.getTextContent()).isNull();
        }

        @Test
        @DisplayName("Empty string returns empty div")
        void emptyString_returnsEmptyDiv() {
            Element result = HtmlParser.parse("");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Whitespace in text content preserved")
        void whitespaceInTextContent_preserved() {
            Element result = HtmlParser.parse("<p>Hello   World</p>");

            assertThat(result.getTextContent()).isEqualTo("Hello   World");
        }

        @Test
        @DisplayName("Whitespace-only HTML returns empty div")
        void whitespaceOnlyHtml_returnsEmptyDiv() {
            Element result = HtmlParser.parse("   \n   \t  ");

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getChildren()).isEmpty();
        }
    }

    // ── Error reporting ──────────────────────────────────────────

    @Nested
    @DisplayName("Error reporting")
    class ErrorReporting {

        @Test
        @DisplayName("Mismatched closing tag throws TemplateException")
        void mismatchedClosingTag_throwsTemplateException() {
            assertThatThrownBy(() -> HtmlParser.parse("<div></span>"))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("</span>")
                    .hasMessageContaining("</div>");
        }

        @Test
        @DisplayName("Mismatched closing tag with template path includes path in error")
        void mismatchedClosingTagWithPath_includesPathInError() {
            assertThatThrownBy(() -> HtmlParser.parse("<div></span>", "pages/test.html"))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("pages/test.html");
        }

        @Test
        @DisplayName("Unclosed tag throws TemplateException")
        void unclosedTag_throwsTemplateException() {
            assertThatThrownBy(() -> HtmlParser.parse("<div><p>text"))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("unclosed");
        }

        @Test
        @DisplayName("Error includes line number for multiline HTML")
        void errorIncludesLineNumber_forMultilineHtml() {
            String html = """
                    <div>
                      <p>ok</p>
                      <span>
                    </div>""";

            assertThatThrownBy(() -> HtmlParser.parse(html, "test.html"))
                    .isInstanceOf(TemplateException.class)
                    .satisfies(ex -> {
                        TemplateException te = (TemplateException) ex;
                        assertThat(te.getTemplatePath()).isEqualTo("test.html");
                        assertThat(te.getLineNumber()).isGreaterThan(0);
                    });
        }
    }

    // ── parse with template path ─────────────────────────────────

    @Nested
    @DisplayName("parse(String, String) overload")
    class ParseWithTemplatePath {

        @Test
        @DisplayName("Null template path does not cause failure on valid HTML")
        void nullTemplatePath_doesNotCauseFailure() {
            Element result = HtmlParser.parse("<div>ok</div>", null);

            assertThat(result.getTag()).isEqualTo("div");
            assertThat(result.getTextContent()).isEqualTo("ok");
        }

        @Test
        @DisplayName("Template path appears in TemplateException on error")
        void templatePath_appearsInExceptionOnError() {
            assertThatThrownBy(() -> HtmlParser.parse("<div></span>", "my/template.html"))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("my/template.html");
        }
    }
}
