package com.warehouse.warehouselab1;

public abstract class Batch{
    private final  int article;
    private final String name;
    private final int qt;
    private final int cell;
    private final String date;

    public  Batch(int article, String name, int qt, int cell, String date){
        this.article = article;
        this.name = name;
        this.qt = qt;
        this.cell = cell;
        this.date = date;
    }

    public int getArticle(){ return article; }
    public String getName(){ return name; }
    public int getQt(){ return qt; }
    public int getCell(){ return cell; }
    public String getDate(){ return date; }

    public String getStatus() {
        if (this instanceof Editable) {
            return "Доступен";
        } else {
            return "В архиве";
        }
    }

    @Override
    public  String toString(){
        return "Партия{" +
                "артикул: " + article +
                " название: " + name +
                " количество: " + qt +
                " ячейка: " + cell +
                " дата завоза: " + date +
                '}';

    }

    public String getCountry() {
        return "—";
    }

    public String getCode() {
        return "—";
    }


}
