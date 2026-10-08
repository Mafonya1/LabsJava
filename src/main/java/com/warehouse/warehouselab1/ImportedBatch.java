package com.warehouse.warehouselab1;

public class ImportedBatch extends Batch implements Editable {
    private String country;
    private String code;

    public ImportedBatch(int article, String name, int qt, int cell, String date, String country, String code) {
        super(article, name, qt, cell, date);
        this.country = country;
        this.code = code;
    }

    public String getCountry(){ return country; }
    public String getCode(){ return code; }

    @Override
    public void update(String newCountry, String newCode) {
        this.country = newCountry;
        this.code = newCode;
        System.out.println("Лог: Поля импорта изменены!");
    }


    @Override
    public String toString() {
        return super.toString() +
                " --> " +
                "{ страна: " + country +
                " код: " + code +
                '}';
    }
}
