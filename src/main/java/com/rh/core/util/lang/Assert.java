package com.rh.core.util.lang;

/**
 * Assertion utility class that assists in validating arguments. Useful for identifying programmer errors early and
 * clearly at runtime.
 * 
 * <p>
 * For example, if the contract of a public method states it does not allow <code>null</code> arguments, Assert can be
 * used to validate that contract. Doing this clearly indicates a contract violation when it occurs and protects the
 * class's invariants.
 * 
 * <p>
 * Typically used to validate method arguments rather than configuration properties, to check for cases that are usually
 * programmer errors rather than configuration errors. In contrast to config initialization code, there is usally no
 * point in falling back to defaults in such methods.
 * 
 * <p>
 * This class is similar to JUnit's assertion library. If an argument value is deemed invalid, an
 * {@link IllegalArgumentException} is thrown (typically). For example:
 * 
 * <pre class="code">
 * Assert.notNull(clazz, &quot;The class must not be null&quot;);
 * Assert.isTrue(i &gt; 0, &quot;The value must be greater than zero&quot;);
 * </pre>
 * 
 * Mainly for internal use within the framework; consider Jakarta's Commons Lang >= 2.0 for a more comprehensive suite
 * of assertion utilities.
 * 
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Rob Harrop
 * @since 1.1.2
 */
public abstract class Assert {

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code> if the test result is
     * <code>false</code>.
     * 
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, &quot;The value must be greater than zero&quot;);
     * </pre>
     * throws IllegalArgumentException if expression is <code>false</code>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * 
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code> if the test result is
     * <code>false</code>.
     * 
     * <pre class="code">
     * Assert.isTrue(i &gt; 0);
     * </pre>
     * throw IllegalArgumentException if expression is <code>false</code>
     * @param expression a boolean expression
     * 
     */
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code> if the test result is
     * <code>true</code>.
     * 
     * <pre class="code">
     * Assert.isFalse(i &gt; 0);
     * </pre>
     * throw IllegalArgumentException if expression is <code>true</code>
     * @param expression a boolean expression
     * 
     */
    public static void isFalse(boolean expression) {
        isTrue(!expression, "[Assertion failed] - this expression must be false");
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code> if the test result is
     * <code>true</code>.
     * 
     * <pre class="code">
     * Assert.isFalse(i &lt;= 0, &quot;The value must be greater than zero&quot;);
     * </pre>
     * throws IllegalArgumentException if expression is <code>true</code>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     */
    public static void isFalse(boolean expression, String message) {
        isTrue(!expression, message);
    }

    /**
     * Assert that an object is <code>null</code> .
     * 
     * <pre class="code">
     * Assert.isNull(value, &quot;The value must be null&quot;);
     * </pre>
     * throws IllegalArgumentException if expression is <code>true</code>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is <code>null</code> .
     * 
     * <pre class="code">
     * Assert.isNull(value);
     * </pre>
     * throws IllegalArgumentException if expression is <code>true</code>
     * @param object the object to check
     */
    public static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * Assert that an object is not <code>null</code> .
     * 
     * <pre class="code">
     * Assert.notNull(clazz, &quot;The class must not be null&quot;);
     * </pre>
     * throws IllegalArgumentException if expression is <code>true</code>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not <code>null</code> .
     * 
     * <pre class="code">
     * Assert.notNull(clazz);
     * </pre>
     * throws IllegalArgumentException if expression is <code>true</code>
     * @param object the object to check
     */
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Assert that the given String is not empty; that is, it must not be <code>null</code> and not the empty String.
     * 
     * <pre class="code">
     * Assert.hasLength(name, &quot;Name must not be empty&quot;);
     * </pre>
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     * @see StringUtils#hasLength
     */
    public static void hasLength(String text, String message) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given String is not empty; that is, it must not be <code>null</code> and not the empty String.
     * 
     * <pre class="code">
     * Assert.hasLength(name);
     * </pre>
     * @param text the String to check
     * @see StringUtils#hasLength
     */
    public static void hasLength(String text) {
        hasLength(text,
                "[Assertion failed] - this String argument must have length; " 
              + "it must not be <code>null</code> or empty");
    }

    /**
     * Assert that the given String has valid text content; that is, it must not be <code>null</code> and must contain
     * at least one non-whitespace character.
     * 
     * <pre class="code">
     * Assert.hasText(name, &quot;'name' must not be empty&quot;);
     * </pre>
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     * @see StringUtils#hasText
     */
    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given String has valid text content; that is, it must not be <code>null</code> and must contain
     * at least one non-whitespace character.
     * 
     * <pre class="code">
     * Assert.hasText(name, &quot;'name' must not be empty&quot;);
     * </pre>
     * @param text the String to check
     * @see StringUtils#hasText
     */
    public static void hasText(String text) {
        hasText(text,
                "[Assertion failed] - this String argument must have text;"
              + " it must not be <code>null</code>, empty, or blank");
    }

    /**
     * Assert that the given text does not contain the given substring.
     * 
     * <pre class="code">
     * Assert.doesNotContain(name, &quot;rod&quot;, &quot;Name must not contain 'rod'&quot;);
     * </pre>
     * @param textToSearch the text to search
     * @param substring the substring to find within the text
     * @param message the exception message to use if the assertion fails
     */
    public static void doesNotContain(String textToSearch, String substring, String message) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) 
                && textToSearch.indexOf(substring) != -1) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given text does not contain the given substring.
     * 
     * <pre class="code">
     * Assert.doesNotContain(name, &quot;rod&quot;);
     * </pre>
     * @param textToSearch the text to search
     * @param substring the substring to find within the text
     */
    public static void doesNotContain(String textToSearch, String substring) {
        doesNotContain(textToSearch, substring,
                "[Assertion failed] - this String argument must not contain the substring [" + substring + "]");
    }

}

/**
 * Miscellaneous {@link String} utility methods.
 * 
 * <p>
 * Mainly for internal use within the framework; consider <a href="http://jakarta.apache.org/commons/lang/">Jakarta's
 * Commons Lang</a> for a more comprehensive suite of {@link org.apache.commons.lang.StringUtils String utilities}.
 * 
 * <p>
 * This class delivers some simple functionality that should really be provided by the core Java <code>String</code> and
 * {@link StringBuffer} classes, such as the ability to {@link #replace} all occurrences of a given substring in a
 * target string. It also provides easy-to-use methods to convert between delimited strings, such as CSV strings, and
 * collections and arrays.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Rick Evans
 * @author Jacky.Song
 * @since 16 April 2001
 */
abstract class StringUtils {

    /**
     * Check that the given String is neither <code>null</code> nor of length 0. Note: Will return <code>true</code> for
     * a String that purely consists of whitespace.
     * <p>
     * 
     * <pre>
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     * @param str the String to check (may be <code>null</code>)
     * @return <code>true</code> if the String is not null and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Check whether the given String has actual text. More specifically, returns <code>true</code> if the string not
     * <code>null</code>, its length is greater than 0, and it contains at least one non-whitespace character.
     * <p>
     * 
     * <pre>
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     * @param str the String to check (may be <code>null</code>)
     * @return <code>true</code> if the String is not <code>null</code>, its length is greater than 0, and it does not
     *         contain whitespace only
     * @see java.lang.Character#isWhitespace
     */
    public static boolean hasText(String str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given String contains any whitespace characters.
     * @param str the String to check (may be <code>null</code>)
     * @return <code>true</code> if the String is not empty and contains at least 1 whitespace character
     * @see java.lang.Character#isWhitespace
     */
    public static boolean containsWhitespace(String str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Capitalize a <code>String</code>, changing the first letter to upper case as per
     * {@link Character#toUpperCase(char)}. No other letters are changed.
     * @param str the String to capitalize, may be <code>null</code>
     * @return the capitalized String, <code>null</code> if null
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * Uncapitalize a <code>String</code>, changing the first letter to lower case as per
     * {@link Character#toLowerCase(char)}. No other letters are changed.
     * @param str the String to uncapitalize, may be <code>null</code>
     * @return the uncapitalized String, <code>null</code> if null
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }
    
    /**
     * 
     * @param str 需要被修改的字符串
     * @param capitalize  改成大写的？ true为大写，false为小写。
     * @return 将第一个字符改成大写或小写格式
     */
    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (str == null || str.length() == 0) {
            return str;
        }
        StringBuffer buf = new StringBuffer(str.length());
        if (capitalize) {
            buf.append(Character.toUpperCase(str.charAt(0)));
        } else {
            buf.append(Character.toLowerCase(str.charAt(0)));
        }
        buf.append(str.substring(1));
        return buf.toString();
    }

}
