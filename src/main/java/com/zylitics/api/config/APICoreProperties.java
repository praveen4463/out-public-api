package com.zylitics.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * All setters in this class allow only first time access by container, after that no values can
 * be mutated.
 * @author Praveen Tiwari
 *
 */
@ThreadSafe
@Component
@ConfigurationProperties(prefix="api-core")
@Validated
@SuppressWarnings("unused")
public class APICoreProperties {
  
  @NotBlank
  private String projectId;
  
  public String getProjectId() {
    return projectId;
  }
  
  public void setProjectId(String projectId) {
    if (this.projectId == null) {
      this.projectId = projectId;
    }
  }
  
  @NotBlank
  private String kmsProjectId;
  
  public String getKmsProjectId() {
    return kmsProjectId;
  }
  
  public void setKmsProjectId(String kmsProjectId) {
    if (this.kmsProjectId == null) {
      this.kmsProjectId = kmsProjectId;
    }
  }
  
  @Valid
  private final DataSource dataSource = new DataSource();
  
  public DataSource getDataSource() { return dataSource; }
  
  @Valid
  private final CloudKms cloudKms = new CloudKms();
  
  public CloudKms getCloudKms() { return cloudKms; }
  
  @Valid
  private final Storage storage = new Storage();
  
  public Storage getStorage() {return storage;}
  
  @Valid
  private final Services services = new Services();
  
  public Services getServices() { return services; }
  
  public static class DataSource {
    
    @NotBlank
    private String dbName;
    
    @NotBlank
    private String userName;
    
    @NotBlank
    private String userSecretCloudFile;
    
    @NotBlank
    private String privateHostCloudFile;
    
    @Min(1)
    private Short minIdleConnPool;
    
    public String getDbName() {
      return dbName;
    }
    
    public void setDbName(String dbName) {
      if (this.dbName == null) {
        this.dbName = dbName;
      }
    }
    
    public String getUserName() {
      return userName;
    }
    
    public void setUserName(String userName) {
      if (this.userName == null) {
        this.userName = userName;
      }
    }
    
    public String getUserSecretCloudFile() {
      return userSecretCloudFile;
    }
    
    public void setUserSecretCloudFile(String userSecretCloudFile) {
      if (this.userSecretCloudFile == null) {
        this.userSecretCloudFile = userSecretCloudFile;
      }
    }
    
    public String getPrivateHostCloudFile() {
      return privateHostCloudFile;
    }
    
    public void setPrivateHostCloudFile(String privateHostCloudFile) {
      if (this.privateHostCloudFile == null) {
        this.privateHostCloudFile = privateHostCloudFile;
      }
    }
    
    public Short getMinIdleConnPool() {
      return minIdleConnPool;
    }
    
    public void setMinIdleConnPool(Short minIdleConnPool) {
      if (this.minIdleConnPool == null) {
        this.minIdleConnPool = minIdleConnPool;
      }
    }
  }
  
  public static class CloudKms {
    
    @NotBlank
    private String keyRing;
    
    @NotBlank
    private String key;
    
    @NotBlank
    private String keyBucket;
    
    public String getKeyRing() {
      return keyRing;
    }
    
    public void setKeyRing(String keyRing) {
      if (this.keyRing == null) {
        this.keyRing = keyRing;
      }
    }
    
    public String getKey() {
      return key;
    }
    
    public void setKey(String key) {
      if (this.key == null) {
        this.key = key;
      }
    }
    
    public String getKeyBucket() {
      return keyBucket;
    }
    
    public void setKeyBucket(String keyBucket) {
      if (this.keyBucket == null) {
        this.keyBucket = keyBucket;
      }
    }
  }
  
  public static class Storage {
    
    @NotBlank
    private String shotBucketUsc;
  
    public String getShotBucketUsc() {
      return shotBucketUsc;
    }
  
    public void setShotBucketUsc(String shotBucketUsc) {
      if (this.shotBucketUsc == null) {
        this.shotBucketUsc = shotBucketUsc;
      }
    }
  }
  
  public static class Services {
    
    @NotBlank
    private String wzgpEndpoint;
    
    @NotBlank
    private String wzgpVersion;
    
    @NotBlank
    private String btbrVersion;
    
    @NotBlank
    private String wzgpAuthUser;
    
    @NotBlank
    private String wzgpAuthSecretCloudFile;
    
    @NotBlank
    private String vmMachineType;
    
    @NotEmpty
    private Set<String> vmZones;
    
    @Min(1)
    private Integer btbrPort;
    
    @NotBlank
    private String btbrAuthUser;
    
    @NotBlank
    private String btbrAuthSecretCloudFile;
    
    @NotBlank
    private String localVmEnvVar;
    
    public String getWzgpEndpoint() {
      return wzgpEndpoint;
    }
    
    public void setWzgpEndpoint(String wzgpEndpoint) {
      if (this.wzgpEndpoint == null) {
        this.wzgpEndpoint = wzgpEndpoint;
      }
    }
    
    public String getWzgpVersion() {
      return wzgpVersion;
    }
    
    public void setWzgpVersion(String wzgpVersion) {
      if (this.wzgpVersion == null) {
        this.wzgpVersion = wzgpVersion;
      }
    }
    
    public String getWzgpAuthUser() {
      return wzgpAuthUser;
    }
    
    public void setWzgpAuthUser(String wzgpAuthUser) {
      if (this.wzgpAuthUser == null) {
        this.wzgpAuthUser = wzgpAuthUser;
      }
    }
    
    public String getWzgpAuthSecretCloudFile() {
      return wzgpAuthSecretCloudFile;
    }
    
    public void setWzgpAuthSecretCloudFile(String wzgpAuthSecretCloudFile) {
      if (this.wzgpAuthSecretCloudFile == null) {
        this.wzgpAuthSecretCloudFile = wzgpAuthSecretCloudFile;
      }
    }
    
    public String getVmMachineType() {
      return vmMachineType;
    }
    
    public void setVmMachineType(String vmMachineType) {
      if (this.vmMachineType == null) {
        this.vmMachineType = vmMachineType;
      }
    }
    
    public Set<String> getVmZones() {
      return vmZones;
    }
    
    public void setVmZones(Set<String> vmZones) {
      if (this.vmZones == null) {
        this.vmZones = vmZones;
      }
    }
    
    public String getBtbrVersion() {
      return btbrVersion;
    }
    
    public void setBtbrVersion(String btbrVersion) {
      if (this.btbrVersion == null) {
        this.btbrVersion = btbrVersion;
      }
    }
    
    public int getBtbrPort() {
      return btbrPort;
    }
    
    public void setBtbrPort(Integer btbrPort) {
      if (this.btbrPort == null) {
        this.btbrPort = btbrPort;
      }
    }
    
    public String getBtbrAuthUser() {
      return btbrAuthUser;
    }
    
    public void setBtbrAuthUser(String btbrAuthUser) {
      if (this.btbrAuthUser == null) {
        this.btbrAuthUser = btbrAuthUser;
      }
    }
    
    public String getBtbrAuthSecretCloudFile() {
      return btbrAuthSecretCloudFile;
    }
    
    public void setBtbrAuthSecretCloudFile(String btbrAuthSecretCloudFile) {
      if (this.btbrAuthSecretCloudFile == null) {
        this.btbrAuthSecretCloudFile = btbrAuthSecretCloudFile;
      }
    }
    
    public String getLocalVmEnvVar() {
      return localVmEnvVar;
    }
    
    public void setLocalVmEnvVar(String localVmEnvVar) {
      if (this.localVmEnvVar == null) {
        this.localVmEnvVar = localVmEnvVar;
      }
    }
  }
}
