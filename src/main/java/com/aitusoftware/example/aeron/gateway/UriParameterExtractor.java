package com.aitusoftware.example.aeron.gateway;

import lombok.val;

import java.util.regex.Pattern;

final class UriParameterExtractor
{
    static long extractNumberParam(final String input, final String paramName)
    {
        val matcher = Pattern.compile(String.format("%s=([0-9]+)", paramName)).matcher(input);
        if (matcher.find())
        {
            return Long.parseLong(matcher.group(1));
        }

        return Long.MIN_VALUE;
    }
}
