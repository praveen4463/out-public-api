package com.zylitics.api.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zylitics.api.model.IncomingFile;
import com.zylitics.api.model.IncomingTest;

import java.util.ArrayList;
import java.util.List;

public class FilesParser {
  
  public List<IncomingFile> parse(List<Object> files) {
    List<IncomingFile> incomingFiles = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      for (Object f : files) {
        IncomingFile incomingFile = new IncomingFile();
        if (f instanceof String) {
          incomingFile.setName(f.toString());
          incomingFiles.add(incomingFile);
          continue;
        }
        File file = mapper.readValue(f.toString(), File.class);
        List<IncomingTest> incomingTests = new ArrayList<>();
        for (Object t : file.getTests()) {
          IncomingTest incomingTest = new IncomingTest();
          if (t instanceof String) {
            incomingTest.setName(t.toString());
            incomingTests.add(incomingTest);
            continue;
          }
          Test test = mapper.readValue(t.toString(), Test.class);
          incomingTest
              .setName(test.getName())
              .setVersions(test.getVersions());
          incomingTests.add(incomingTest);
        }
        incomingFile
            .setName(file.getName())
            .setTests(incomingTests);
        incomingFiles.add(incomingFile);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return incomingFiles;
  }

  private static class File {
  
    private String name;
  
    private List<Object> tests;
  
    public String getName() {
      return name;
    }
  
    public File setName(String name) {
      this.name = name;
      return this;
    }
  
    public List<Object> getTests() {
      return tests;
    }
  
    public File setTests(List<Object> tests) {
      this.tests = tests;
      return this;
    }
  }
  
  private static class Test {
    
    private String name;
    
    private List<String> versions;
  
    public String getName() {
      return name;
    }
  
    public Test setName(String name) {
      this.name = name;
      return this;
    }
  
    public List<String> getVersions() {
      return versions;
    }
  
    public Test setVersions(List<String> versions) {
      this.versions = versions;
      return this;
    }
  }
}
