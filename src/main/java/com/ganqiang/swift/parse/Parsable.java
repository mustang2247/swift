package com.ganqiang.swift.parse;

import java.util.List;

import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.storage.Result;

public interface Parsable
{
  List<Result> parse(Event event);

}
