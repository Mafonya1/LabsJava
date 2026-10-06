package com.warehouse.warehouselab1;

public class Batch{
    private  int article;
    private  String name;
    private int qt;
    private int cell;
    private String date;

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

    public void setName(String newName){ this.name = newName; }
    public void setCell(int newCell){ this.cell = newCell; }

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
}
