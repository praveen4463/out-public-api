package com.zylitics.api;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.zylitics.api.config.APICoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

// TODO: We shouldn't use secrets in local as it is in btbr, but here due to email service and it's
//  usage locally we've to use it.
@Service
class CloudKMSSecretsManager implements SecretsManager {
  
  private final APICoreProperties apiCoreProperties;
  private final Storage storage;
  
  private KeyManagementServiceClient client;
  
  private boolean isClosed = false;
  
  @SuppressWarnings("unused")
  @Autowired
  CloudKMSSecretsManager(APICoreProperties apiCoreProperties, Storage storage)
      throws IOException {
    this(apiCoreProperties, storage, KeyManagementServiceClient.create());
  }
  
  CloudKMSSecretsManager(APICoreProperties apiCoreProperties, Storage storage,
                         KeyManagementServiceClient client) {
    this.apiCoreProperties = apiCoreProperties;
    this.storage = storage;
    this.client = client;
  }
  
  @Override
  public String getSecretAsPlainText(String secretCloudFileName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(secretCloudFileName),
        "secret cloud file name can't be empty");
    
    APICoreProperties.CloudKms kms = apiCoreProperties.getCloudKms();
    BlobId blobId = BlobId.of(kms.getKeyBucket(), secretCloudFileName);
    // we'll throw if there is any error for now as storage and kms both have retry built-in and
    // i don't expect storage to throw any error that needs retry from user while 'getting' blob.
    byte[] content = storage.readAllBytes(blobId);
    String resourceName = CryptoKeyName.format(apiCoreProperties.getKmsProjectId(), "global",
        kms.getKeyRing(), kms.getKey());
    DecryptResponse decrypt = client.decrypt(resourceName, ByteString.copyFrom(content));
    // trim is important to remove unintended whitespaces.
    return decrypt.getPlaintext().toStringUtf8().trim();
  }
  
  @Override
  public void reAcquireClientAfterClose() throws IOException {
    if (isClosed) {
      client = KeyManagementServiceClient.create();
      isClosed = false;
    }
  }
  
  @Override
  public void close() {
    client.close();
    isClosed = true;
  }
}
