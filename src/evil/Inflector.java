package evil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author participant
 * 
 * @see <a
 *      href="http://www.java2s.com/Open-Source/Android/Framework/dynadroid/org/dynadroid/utils/Inflector.java.htm">source</a>
 */
public class Inflector {

    private static final class RuleAndReplacement {
        private final String rule;

        private final String replacement;

        public RuleAndReplacement(final String rule, final String replacement) {
            this.rule = rule;
            this.replacement = replacement;
        }

        public String getReplacement() {
            return replacement;
        }

        public String getRule() {
            return rule;
        }
    }

    private static final Pattern UNDERSCORE_PATTERN_1 = Pattern.compile("([A-Z]+)([A-Z][a-z])");

    private static final Pattern UNDERSCORE_PATTERN_2 = Pattern.compile("([a-z\\d])([A-Z])");

    private static List<RuleAndReplacement> plurals = new ArrayList<RuleAndReplacement>();

    private static List<RuleAndReplacement> singulars = new ArrayList<RuleAndReplacement>();

    private static List<String> uncountables = new ArrayList<String>();

    static {
        plural("$", "s");
        plural("s$", "s");
        plural("(ax|test)is$", "$1es");
        plural("(octop|vir)us$", "$1i");
        plural("(alias|status)$", "$1es");
        plural("(bu)s$", "$1es");
        plural("(buffal|tomat)o$", "$1oes");
        plural("([ti])um$", "$1a");
        plural("sis$", "ses");
        plural("(?:([^f])fe|([lr])f)$", "$1$2ves");
        plural("(hive)$", "$1s");
        plural("([^aeiouy]|qu)y$", "$1ies");
        plural("([^aeiouy]|qu)ies$", "$1y");
        plural("(x|ch|ss|sh)$", "$1es");
        plural("(matr|vert|ind)ix|ex$", "$1ices");
        plural("([m|l])ouse$", "$1ice");
        plural("(ox)$", "$1en");
        plural("(quiz)$", "$1zes");
        singular("s$", "");
        singular("(n)ews$", "$1ews");
        singular("([ti])a$", "$1um");
        singular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis");
        singular("(^analy)ses$", "$1sis");
        singular("([^f])ves$", "$1fe");
        singular("(hive)s$", "$1");
        singular("(tive)s$", "$1");
        singular("([lr])ves$", "$1f");
        singular("([^aeiouy]|qu)ies$", "$1y");
        singular("(s)eries$", "$1eries");
        singular("(m)ovies$", "$1ovie");
        singular("(x|ch|ss|sh)es$", "$1");
        singular("([m|l])ice$", "$1ouse");
        singular("(bus)es$", "$1");
        singular("(o)es$", "$1");
        singular("(shoe)s$", "$1");
        singular("(cris|ax|test)es$", "$1is");
        singular("([octop|vir])i$", "$1us");
        singular("(alias|status)es$", "$1");
        singular("^(ox)en", "$1");
        singular("(vert|ind)ices$", "$1ex");
        singular("(matr)ices$", "$1ix");
        singular("(quiz)zes$", "$1");
        irregular("person", "people");
        irregular("man", "men");
        irregular("child", "children");
        irregular("sex", "sexes");
        irregular("move", "moves");
        uncountable(new String[]{"equipment", "information", "rice", "money", "species", "series", "fish", "sheep"});
    }

    public static String camelCase(final String name) {
        final StringBuilder builder = new StringBuilder();
        for (final String part : name.split("_")) {
            builder.append(Character.toTitleCase(part.charAt(0)))
                    .append(part.substring(1));
        }
        return builder.toString();
    }

    public static void irregular(final String singular, final String plural) {
        plural(singular + "$", plural);
        singular(plural + "$", singular);
    }

    public static void plural(final String rule, final String replacement) {
        plurals.add(0, new RuleAndReplacement(rule, replacement));
    }

    public static void singular(final String rule, final String replacement) {
        singulars.add(0, new RuleAndReplacement(rule, replacement));
    }

    public static void uncountable(final String... words) {
        uncountables.addAll(Arrays.asList(words));
    }

    public static String pluralize(final String word) {
        if (uncountables.contains(word.toLowerCase())) {
            return word;
        }
        return replaceWithFirstRule(word, plurals);
    }

    private static String replaceWithFirstRule(final String word, final List<RuleAndReplacement> ruleAndReplacements) {
        for (final RuleAndReplacement rar : ruleAndReplacements) {
            final String rule = rar.getRule();
            final String replacement = rar.getReplacement();
            final Matcher matcher = Pattern.compile(rule, Pattern.CASE_INSENSITIVE)
                    .matcher(word);
            if (matcher.find()) {
                return matcher.replaceAll(replacement);
            }
        }
        return word;
    }

    public static String singularize(final String word) {
        if (uncountables.contains(word.toLowerCase())) {
            return word;
        }
        return replaceWithFirstRule(word, singulars);
    }

    public static String tableize(final Class<?> klass) {
        return tableize(klass.getSimpleName());
    }

    public static String tableize(final String className) {
        return pluralize(underscore(className));
    }

    public static String underscore(final String camelCasedWord) {
        String underscoredWord = UNDERSCORE_PATTERN_1.matcher(camelCasedWord)
                .replaceAll("$1_$2");
        underscoredWord = UNDERSCORE_PATTERN_2.matcher(underscoredWord)
                .replaceAll("$1_$2");
        underscoredWord = underscoredWord.replace('-', '_')
                .toLowerCase();
        return underscoredWord;
    }
}
