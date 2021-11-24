package com.asset.migration.query;

import lombok.Data;

@Data
public class BSValidation implements IBSValidation{
    boolean mandatory;
    String regex;
    String query;
}
