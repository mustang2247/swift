package com.ganqiang.swift.storage;

public interface Storable
{

  <Store> Result readOne(Store... s);

  <Store> void writeBatch(Store... s);

}
