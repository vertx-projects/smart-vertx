package com.smart.vertx.entity.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = true)
public class PageCommand extends BaseCommand {
    private static final long serialVersionUID = 1L;
    private int pageSize;
    private int pageIndex;
}
