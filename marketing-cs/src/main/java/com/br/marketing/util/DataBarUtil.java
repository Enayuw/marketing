package com.br.marketing.util;

public class DataBarUtil {

    public static String buildNumTypeConditionalFormatting(String min, String max,String region) {

        return "<x14:conditionalFormatting>\n" +
                "  <x14:cfRule type=\"dataBar\" id=\"{00000000-000E-0000-0000-000001000000}\">\n" +
                "    <x14:dataBar minLength=\"0\" maxLength=\"100\" gradient=\"true\">\n" +
                "      <x14:cfvo type=\"num\"><xm:f>" + min + "</xm:f></x14:cfvo>\n" +
                "      <x14:cfvo type=\"num\"><xm:f>" + max + "</xm:f></x14:cfvo>\n" +
                "    </x14:dataBar>\n" +
                "    <xm:sqref>" + region + "</xm:sqref>\n" +
                "  </x14:cfRule>\n" +
                "</x14:conditionalFormatting>";
    }

    public static String buildMinAndMaxTypeConditionalFormatting(String region) {
        return "<x14:conditionalFormatting>\n" +
                "  <x14:cfRule type=\"dataBar\" id=\"{00000000-000E-0000-0000-000001000000}\">\n" +
                "    <x14:dataBar minLength=\"0\" maxLength=\"100\" gradient=\"true\">\n" +
                "      <x14:cfvo type=\"min\"></x14:cfvo>\n" +
                "      <x14:cfvo type=\"max\"></x14:cfvo>\n" +
                "    </x14:dataBar>\n" +
                "    <xm:sqref>" + region + "</xm:sqref>\n" +
                "  </x14:cfRule>\n" +
                "</x14:conditionalFormatting>";
    }


}
