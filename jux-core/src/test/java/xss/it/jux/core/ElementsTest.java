package xss.it.jux.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for every static factory method in the {@link Elements} utility class.
 *
 * <p>Verifies that each factory produces an {@link Element} with the correct
 * HTML tag name. Also tests the ADA-enforced image factories, the skip-nav
 * helper, the screen-reader-only helper, and the live-region helper.</p>
 */
class ElementsTest {

    // ── Semantic Structure ────────────────────────────────────────────

    @Nested
    @DisplayName("Semantic structure elements")
    class Semantic {

        @Test void headerTag()     { assertThat(header().getTag()).isEqualTo("header"); }
        @Test void navTag()        { assertThat(nav().getTag()).isEqualTo("nav"); }
        @Test void mainTag()       { assertThat(main_().getTag()).isEqualTo("main"); }
        @Test void asideTag()      { assertThat(aside().getTag()).isEqualTo("aside"); }
        @Test void footerTag()     { assertThat(footer().getTag()).isEqualTo("footer"); }
        @Test void sectionTag()    { assertThat(section().getTag()).isEqualTo("section"); }
        @Test void articleTag()    { assertThat(article().getTag()).isEqualTo("article"); }
        @Test void figureTag()     { assertThat(figure().getTag()).isEqualTo("figure"); }
        @Test void figcaptionTag() { assertThat(figcaption().getTag()).isEqualTo("figcaption"); }
        @Test void detailsTag()    { assertThat(details().getTag()).isEqualTo("details"); }
        @Test void summaryTag()    { assertThat(summary().getTag()).isEqualTo("summary"); }
        @Test void dialogTag()     { assertThat(dialog().getTag()).isEqualTo("dialog"); }
        @Test void searchTag()     { assertThat(search().getTag()).isEqualTo("search"); }
    }

    // ── Headings ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Heading elements")
    class Headings {

        @Test void h1Tag() { assertThat(h1().getTag()).isEqualTo("h1"); }
        @Test void h2Tag() { assertThat(h2().getTag()).isEqualTo("h2"); }
        @Test void h3Tag() { assertThat(h3().getTag()).isEqualTo("h3"); }
        @Test void h4Tag() { assertThat(h4().getTag()).isEqualTo("h4"); }
        @Test void h5Tag() { assertThat(h5().getTag()).isEqualTo("h5"); }
        @Test void h6Tag() { assertThat(h6().getTag()).isEqualTo("h6"); }
    }

    // ── Generic Containers ────────────────────────────────────────────

    @Nested
    @DisplayName("Generic container elements")
    class Generic {

        @Test void divTag()  { assertThat(div().getTag()).isEqualTo("div"); }
        @Test void spanTag() { assertThat(span().getTag()).isEqualTo("span"); }
    }

    // ── Text & Inline Elements ────────────────────────────────────────

    @Nested
    @DisplayName("Text and inline elements")
    class TextElements {

        @Test void pTag()          { assertThat(p().getTag()).isEqualTo("p"); }
        @Test void aTag()          { assertThat(a().getTag()).isEqualTo("a"); }
        @Test void strongTag()     { assertThat(strong().getTag()).isEqualTo("strong"); }
        @Test void emTag()         { assertThat(em().getTag()).isEqualTo("em"); }
        @Test void smallTag()      { assertThat(small().getTag()).isEqualTo("small"); }
        @Test void markTag()       { assertThat(mark().getTag()).isEqualTo("mark"); }
        @Test void codeTag()       { assertThat(code().getTag()).isEqualTo("code"); }
        @Test void preTag()        { assertThat(pre().getTag()).isEqualTo("pre"); }
        @Test void blockquoteTag() { assertThat(blockquote().getTag()).isEqualTo("blockquote"); }
        @Test void timeTag()       { assertThat(time().getTag()).isEqualTo("time"); }
        @Test void abbrTag()       { assertThat(abbr().getTag()).isEqualTo("abbr"); }
    }

    // ── Lists ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("List elements")
    class Lists {

        @Test void ulTag() { assertThat(ul().getTag()).isEqualTo("ul"); }
        @Test void olTag() { assertThat(ol().getTag()).isEqualTo("ol"); }
        @Test void liTag() { assertThat(li().getTag()).isEqualTo("li"); }
        @Test void dlTag() { assertThat(dl().getTag()).isEqualTo("dl"); }
        @Test void dtTag() { assertThat(dt().getTag()).isEqualTo("dt"); }
        @Test void ddTag() { assertThat(dd().getTag()).isEqualTo("dd"); }
    }

    // ── Tables ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Table elements")
    class Tables {

        @Test void tableTag()   { assertThat(table().getTag()).isEqualTo("table"); }
        @Test void theadTag()   { assertThat(thead().getTag()).isEqualTo("thead"); }
        @Test void tbodyTag()   { assertThat(tbody().getTag()).isEqualTo("tbody"); }
        @Test void tfootTag()   { assertThat(tfoot().getTag()).isEqualTo("tfoot"); }
        @Test void trTag()      { assertThat(tr().getTag()).isEqualTo("tr"); }
        @Test void thTag()      { assertThat(th().getTag()).isEqualTo("th"); }
        @Test void tdTag()      { assertThat(td().getTag()).isEqualTo("td"); }
        @Test void captionTag() { assertThat(caption().getTag()).isEqualTo("caption"); }
    }

    // ── Forms ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Form elements")
    class Forms {

        @Test void formTag()     { assertThat(form().getTag()).isEqualTo("form"); }
        @Test void inputTag()    { assertThat(input().getTag()).isEqualTo("input"); }
        @Test void textareaTag() { assertThat(textarea().getTag()).isEqualTo("textarea"); }
        @Test void selectTag()   { assertThat(select().getTag()).isEqualTo("select"); }
        @Test void optionTag()   { assertThat(option().getTag()).isEqualTo("option"); }
        @Test void optgroupTag() { assertThat(optgroup().getTag()).isEqualTo("optgroup"); }
        @Test void buttonTag()   { assertThat(button().getTag()).isEqualTo("button"); }
        @Test void labelTag()    { assertThat(label().getTag()).isEqualTo("label"); }
        @Test void fieldsetTag() { assertThat(fieldset().getTag()).isEqualTo("fieldset"); }
        @Test void legendTag()   { assertThat(legend().getTag()).isEqualTo("legend"); }
        @Test void outputTag()   { assertThat(output().getTag()).isEqualTo("output"); }
        @Test void progressTag() { assertThat(progress().getTag()).isEqualTo("progress"); }
        @Test void meterTag()    { assertThat(meter().getTag()).isEqualTo("meter"); }
    }

    // ── Media ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Media elements")
    class Media {

        @Test void pictureTag() { assertThat(picture().getTag()).isEqualTo("picture"); }
        @Test void sourceTag()  { assertThat(source().getTag()).isEqualTo("source"); }
        @Test void videoTag()   { assertThat(video().getTag()).isEqualTo("video"); }
        @Test void audioTag()   { assertThat(audio().getTag()).isEqualTo("audio"); }
        @Test void trackTag()   { assertThat(track().getTag()).isEqualTo("track"); }
        @Test void canvasTag()  { assertThat(canvas().getTag()).isEqualTo("canvas"); }
        @Test void svgTag()     { assertThat(svg().getTag()).isEqualTo("svg"); }
        @Test void iframeTag()  { assertThat(iframe().getTag()).isEqualTo("iframe"); }
    }

    // ── Misc ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Misc elements")
    class Misc {

        @Test void hrTag()  { assertThat(hr().getTag()).isEqualTo("hr"); }
        @Test void brTag()  { assertThat(br().getTag()).isEqualTo("br"); }
        @Test void wbrTag() { assertThat(wbr().getTag()).isEqualTo("wbr"); }
    }

    // ── ADA-Enforced Images ───────────────────────────────────────────

    @Nested
    @DisplayName("ADA-enforced image factories")
    class Images {

        @Test
        @DisplayName("img(src, alt) sets src and alt attributes")
        void imgWithAlt() {
            Element el = img("photo.jpg", "A photo");
            assertThat(el.getTag()).isEqualTo("img");
            assertThat(el.getAttributes().get("src")).isEqualTo("photo.jpg");
            assertThat(el.getAttributes().get("alt")).isEqualTo("A photo");
        }

        @Test
        @DisplayName("imgDecorative(src) sets empty alt and role=presentation")
        void imgDecorative() {
            Element el = Elements.imgDecorative("bg.png");
            assertThat(el.getTag()).isEqualTo("img");
            assertThat(el.getAttributes().get("src")).isEqualTo("bg.png");
            assertThat(el.getAttributes().get("alt")).isEmpty();
            assertThat(el.getAttributes().get("role")).isEqualTo("presentation");
        }
    }

    // ── Accessibility Helpers ─────────────────────────────────────────

    @Nested
    @DisplayName("Accessibility helper factories")
    class A11yHelpers {

        @Test
        @DisplayName("skipNav creates a link with jux-skip-nav class and href anchor")
        void skipNavCreatesLink() {
            Element el = skipNav("main-content", "Skip to main");
            assertThat(el.getTag()).isEqualTo("a");
            assertThat(el.getAttributes().get("href")).isEqualTo("#main-content");
            assertThat(el.getCssClasses()).contains("jux-skip-nav");
            assertThat(el.getTextContent()).isEqualTo("Skip to main");
        }

        @Test
        @DisplayName("srOnly creates a span with jux-sr-only class and text")
        void srOnlyCreatesSpan() {
            Element el = srOnly("hidden text");
            assertThat(el.getTag()).isEqualTo("span");
            assertThat(el.getCssClasses()).contains("jux-sr-only");
            assertThat(el.getTextContent()).isEqualTo("hidden text");
        }

        @Test
        @DisplayName("liveRegion creates a div with aria-live, aria-atomic, and jux-live-region class")
        void liveRegionCreatesDiv() {
            Element el = liveRegion("polite");
            assertThat(el.getTag()).isEqualTo("div");
            assertThat(el.getAttributes().get("aria-live")).isEqualTo("polite");
            assertThat(el.getAttributes().get("aria-atomic")).isEqualTo("true");
            assertThat(el.getCssClasses()).contains("jux-live-region");
        }
    }
}
