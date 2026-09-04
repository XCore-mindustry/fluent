package test.functions;

import fluent.bundle.FluentBundle;
import fluent.bundle.FluentFunctionRegistry;
import fluent.bundle.FluentResource;
import fluent.bundle.LRUFunctionCache;
import fluent.syntax.parser.FTLParser;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomFormatterTest {

    interface BaseItem {
        String id();
    }

    static class SpecialItem implements BaseItem {
        private final String id;
        public SpecialItem(String id) { this.id = id; }
        @Override public String id() { return id; }
        @Override public String toString() { return "SpecialItem[" + id + "]"; }
    }

    static class OtherItem {
        private final String val;
        public OtherItem(String val) { this.val = val; }
        @Override public String toString() { return "Other[" + val + "]"; }
    }

    @Test
    public void testExactOnlyCustomFormatter() {
        FluentResource resource = FTLParser.parse("msg = Item: { $item }\n");
        FluentFunctionRegistry registry = FluentFunctionRegistry.builder()
                .addDefaultFormatterExact(SpecialItem.class, (item, scope) -> "EXACT:" + item.id())
                .build();
        FluentBundle bundle = FluentBundle.builder(Locale.ENGLISH, registry, LRUFunctionCache.of())
                .addResource(resource)
                .build();

        String result = bundle.format("msg", Map.of("item", new SpecialItem("sword")));
        assertEquals("Item: EXACT:sword", result);
    }

    @Test
    public void testSubtypeOnlyCustomFormatter() {
        FluentResource resource = FTLParser.parse("msg = Item: { $item }\n");
        FluentFunctionRegistry registry = FluentFunctionRegistry.builder()
                .addDefaultFormatter(BaseItem.class, (item, scope) -> "SUBTYPE:" + item.id())
                .build();
        FluentBundle bundle = FluentBundle.builder(Locale.ENGLISH, registry, LRUFunctionCache.of())
                .addResource(resource)
                .build();

        String result = bundle.format("msg", Map.of("item", new SpecialItem("shield")));
        assertEquals("Item: SUBTYPE:shield", result);
    }

    @Test
    public void testExactWinsOverSubtype() {
        FluentResource resource = FTLParser.parse("msg = Item: { $item }\n");
        FluentFunctionRegistry registry = FluentFunctionRegistry.builder()
                .addDefaultFormatter(BaseItem.class, (item, scope) -> "SUBTYPE:" + item.id())
                .addDefaultFormatterExact(SpecialItem.class, (item, scope) -> "EXACT:" + item.id())
                .build();
        FluentBundle bundle = FluentBundle.builder(Locale.ENGLISH, registry, LRUFunctionCache.of())
                .addResource(resource)
                .build();

        String result = bundle.format("msg", Map.of("item", new SpecialItem("potion")));
        assertEquals("Item: EXACT:potion", result);
    }

    @Test
    public void testFallbackWhenNoCustomMatches() {
        FluentResource resource = FTLParser.parse("msg = Item: { $item }\n");
        FluentFunctionRegistry registry = FluentFunctionRegistry.builder()
                .addDefaultFormatterExact(SpecialItem.class, (item, scope) -> "EXACT:" + item.id())
                .build();
        FluentBundle bundle = FluentBundle.builder(Locale.ENGLISH, registry, LRUFunctionCache.of())
                .addResource(resource)
                .build();

        String result = bundle.format("msg", Map.of("item", new OtherItem("raw")));
        assertEquals("Item: Other[raw]", result);
    }
}
