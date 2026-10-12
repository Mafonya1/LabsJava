package com.warehouse.warehouselab1;

import java.util.List;

public class ReadRes{

    public record LoadResult(List<Batch> batches, int skipped) { }
}
