/*
 * Copyright 2019 liaochong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.liaochong.myexcel.core.converter.writer;

import com.github.liaochong.myexcel.core.annotation.ExcelColumn;
import com.github.liaochong.myexcel.core.cache.WeakCache;
import com.github.liaochong.myexcel.core.container.Pair;
import com.github.liaochong.myexcel.core.converter.WriteConverter;
import com.github.liaochong.myexcel.utils.StringUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * bigdecimal格式化
 *
 * @author liaochong
 * @version 1.0
 */
public class BigDecimalWriteConverter implements WriteConverter {

    private static final WeakCache<String, ThreadLocal<DecimalFormat>> DECIMAL_FORMAT_WEAK_CACHE = new WeakCache<>();

    @Override
    public Pair<Class, Object> convert(Field field, Object fieldVal) {
        String format = field.getAnnotation(ExcelColumn.class).decimalFormat();
        String[] formatSplits = format.split("\\.");
        BigDecimal value = (BigDecimal) fieldVal;
        if (formatSplits.length == 2) {
            value = value.setScale(formatSplits[1].length(), RoundingMode.HALF_UP);
        }
        DecimalFormat decimalFormat = this.getDecimalFormat(format);
        return Pair.of(String.class, decimalFormat.format(value));
    }

    @Override
    public boolean support(Field field, Object fieldVal) {
        Class<?> fieldType = field.getType();
        if (fieldType != BigDecimal.class) {
            return false;
        }
        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
        return StringUtil.isNotBlank(excelColumn.decimalFormat());
    }

    private DecimalFormat getDecimalFormat(String decimalFormat) {
        ThreadLocal<DecimalFormat> tl = DECIMAL_FORMAT_WEAK_CACHE.get(decimalFormat);
        if (tl == null) {
            tl = ThreadLocal.withInitial(() -> new DecimalFormat(decimalFormat));
            DECIMAL_FORMAT_WEAK_CACHE.cache(decimalFormat, tl);
        }
        return tl.get();
    }
}
