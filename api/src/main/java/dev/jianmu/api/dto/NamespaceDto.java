package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @class: NamespaceDto
 * @description: 命名空间Dto
 * @author: Ethan Liu
 * @create: 2021-04-20 12:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "命名空间Dto")
public class NamespaceDto {
    @Schema(required = true)
    @NotBlank
    private String name;
    private String description;
}
