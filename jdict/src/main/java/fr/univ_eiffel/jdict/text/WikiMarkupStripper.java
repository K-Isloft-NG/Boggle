package fr.univ_eiffel.jdict.text;

/** Retire les modèles et garde le texte affiché des liens MediaWiki. */
public final class WikiMarkupStripper {
    private WikiMarkupStripper() {
    }

    /** Renvoie le texte visible d'une définition pour le comptage des lettres. */
    public static String strip(String text) {
        String withoutTemplates = removeTemplates(text);
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < withoutTemplates.length()) {
            if (withoutTemplates.startsWith("[[", i)) {
                int end = withoutTemplates.indexOf("]]", i + 2);
                if (end < 0) {
                    result.append(withoutTemplates, i, withoutTemplates.length());
                    break;
                }
                int displayStart = i + 2;
                for (int j = displayStart; j < end; j++) {
                    if (withoutTemplates.charAt(j) == '|') {
                        displayStart = j + 1;
                    }
                }
                result.append(withoutTemplates, displayStart, end);
                i = end + 2;
            } else {
                result.append(withoutTemplates.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private static String removeTemplates(String text) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        int i = 0;
        while (i < text.length()) {
            if (text.startsWith("{{", i)) {
                depth++;
                i += 2;
            } else if (depth > 0 && text.startsWith("}}", i)) {
                depth--;
                i += 2;
            } else {
                // Un modèle non fermé garde la profondeur positive jusqu'à la fin.
                if (depth == 0) {
                    result.append(text.charAt(i));
                }
                i++;
            }
        }
        return result.toString();
    }
}
