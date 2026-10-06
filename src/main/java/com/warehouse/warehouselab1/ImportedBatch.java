package com.warehouse.warehouselab1;

public class ImportedBatch extends Batch implements Editable {
    String country;
    String code;

    public ImportedBatch(int article, String name, int qt, int cell, String date, String country, String code) {
        super(article, name, qt, cell, date);
        this.country = country;
        this.code = code;
    }

    public String getContry(){ return country; }
    public String getCode(){ return code; }

    @Override
    public void update(int newCell, String newName){
        this.setCell(newCell);
        this.setName(newName);
    }


    public String toString() {
        return super.toString() +
                " --> " +
                "{ страна: " + country +
                " код: " + code +
                '}';
    }
}
