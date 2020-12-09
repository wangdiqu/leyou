package com.leyou.item.pojo;

import lombok.Data;
import lombok.Value;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name="tb_specification")
public class SpecIfCations {
     @Id
     private Long category_id;
     private String specifications;
}
