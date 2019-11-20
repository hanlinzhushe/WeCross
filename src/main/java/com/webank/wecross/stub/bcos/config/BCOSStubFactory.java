package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.bcos.BCOSStub;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStubFactory {

    private static Logger logger = LoggerFactory.getLogger(BCOSStubFactory.class);

    public static BCOSStub getBcosStub(
            String networkName, String stubName, String stubPath, Map<String, Object> stubConfig)
            throws WeCrossException {

        @SuppressWarnings("unchecked")
        Map<String, Boolean> guomiMap = (Map<String, Boolean>) stubConfig.get("guomi");
        if (guomiMap == null) {
            String errorMessage = "Something wrong in [guomi] item, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        Boolean guomi = guomiMap.get("enable");
        if (guomi == null) {
            String errorMessage = "\"enable\" in [guomi] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        EncryptType encryptType = guomi ? new EncryptType(1) : new EncryptType(0);

        @SuppressWarnings("unchecked")
        Map<String, String> accountConfig = (Map<String, String>) stubConfig.get("account");
        if (accountConfig == null) {
            String errorMessage = "Something wrong in [account] item, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        Account account = BCOSConfigUtils.getBcosAccount(stubPath, accountConfig);
        account.setEncryptType(encryptType);

        @SuppressWarnings("unchecked")
        Map<String, Object> channelServiceConfig =
                (Map<String, Object>) stubConfig.get("channelService");
        if (channelServiceConfig == null) {
            String errorMessage =
                    "Something wrong in [channelService] item, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        ChannelService channelService =
                BCOSConfigUtils.getBcosChannelService(stubPath, channelServiceConfig);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> resources =
                (List<Map<String, String>>) stubConfig.get("resources");

        if (resources == null) {
            String warnMessage = "\"resources\" of" + stubName + "  not found.";
            logger.warn(warnMessage);

            return BCOSStubFactory.initBCOSStub(
                    networkName, stubName, stubPath, account, channelService, null);
        } else {
            return BCOSStubFactory.initBCOSStub(
                    networkName, stubName, stubPath, account, channelService, resources);
        }
    }

    public static BCOSStub initBCOSStub(
            String networkName,
            String stubName,
            String stubPath,
            Account account,
            ChannelService channelService,
            List<Map<String, String>> resources)
            throws WeCrossException {
        Credentials credentials = account.getCredentials();

        Web3SdkFactory web3SdkFactory = new Web3SdkFactory(credentials, channelService);
        Web3Sdk web3Sdk = web3SdkFactory.getWeb3Sdk(stubName);

        // init bcos stub
        BCOSStub bcosStub = new BCOSStub();
        bcosStub.setBcosService(web3Sdk.getBcosService());
        bcosStub.setWeb3(web3Sdk.getWeb3());
        bcosStub.setCredentials(web3Sdk.getCredentials());

        // init bcos resources
        String prefix = networkName + "." + stubName;
        Map<String, Resource> bcosResources =
                BCOSConfigUtils.getBcosResources(prefix, stubPath, resources, web3Sdk.getWeb3());
        bcosStub.setResources(bcosResources);

        logger.debug("Init {}.{} finished", networkName, stubName);
        return bcosStub;
    }
}
