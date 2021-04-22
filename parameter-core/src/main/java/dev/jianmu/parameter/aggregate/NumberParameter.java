package dev.jianmu.parameter.aggregate;

import java.math.BigDecimal;

/**
 * @class: NumberParameter
 * @description: 数值参数
 * @author: Ethan Liu
 * @create: 2021-04-20 22:57
 **/
public class NumberParameter extends Parameter<BigDecimal> {
    public NumberParameter(BigDecimal value) {
        super(value);
        this.type = Type.NUMBER;
    }

    @Override
    public String getStringValue() {
        return value.toPlainString();
    }
}
