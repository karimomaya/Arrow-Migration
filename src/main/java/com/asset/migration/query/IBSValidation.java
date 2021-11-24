package com.asset.migration.query;

import com.asset.migration.config.ColumnConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = BSValidation.class)
public interface IBSValidation {
}
