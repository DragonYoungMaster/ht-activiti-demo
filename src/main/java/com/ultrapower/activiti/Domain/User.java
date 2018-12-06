package com.ultrapower.activiti.Domain;

import java.io.Serializable;
import java.util.Date;

/**
 * author: teng.he
 * time: 11:43 2018/12/6
 * desc:
 */
public class User implements Serializable{
  private static final long serialVersionUID = -2636458084698628232L;
  private String id;
  private String name;
  private Date time;
  private String note;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
