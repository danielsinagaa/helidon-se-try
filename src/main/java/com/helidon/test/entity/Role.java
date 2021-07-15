package com.helidon.test.entity;

import io.helidon.common.Reflected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Reflected
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private int id;
    private String name;
}
