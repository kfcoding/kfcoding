package com.cuiyun.kfcoding.rest.modular.cloudware;

import com.google.gson.Gson;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;

public class K8sApi {
    // URL and TOKEN will be deleted in release
    private final static String URL = "https://120.132.94.141:6443";
    private final static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLWo3cjZrIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI0NmVlN2VmOC00YWFkLTExZTgtYmEwYi01MjU0MDAzMjgyNjUiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZS1zeXN0ZW06YWRtaW4tdXNlciJ9.Fx4GIjOtOTJA2u4vLsl6mMDcpD5cqW31ErwR-7fvEfN_2oMBiJUSE75V-EAngiZpyxlCyjUahXfq1T2qWJhEWSvdzNOJBbbQSVDAcRgHUC7QT9QIZS0si5rwBBJUCz92k_55BltYI9oa9HEFp3u41BrK73714xWXbdMWhev2YNAWIeGFB0BA2SSTHEByTfaXka5eslTYDg23j8-qs_JmLjQPvxcPXxh_vu89zxkKzDjwpVy-EkcQr5r-ubW03qku7xHfDVMc2MVepZOS-VJbnUcGNGNBvGQtItdeJsUdx18gJ7ABERE-h9FtMvBCnH_Yilg7wJM3_iC9W6ilDHCRPg";

    private static K8sApi instance = new K8sApi();
    private CoreV1Api coreV1Api;
    private ExtensionsV1beta1Api extensionsV1beta1Api;

    private K8sApi() {
        ApiClient client = null;
        try {
            client = Config.fromToken(URL, TOKEN, false);
            //client = Config.fromCluster();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);

        coreV1Api = new CoreV1Api();
        extensionsV1beta1Api = new ExtensionsV1beta1Api();
    }

    public static K8sApi getInstance() {
        return instance;
    }

    /**
     * create terminal pod
     *
     * @param namespace
     * @param podName
     * @param imageName
     * @return
     */
    public V1Pod createTerminalPod(String namespace, String podName, String imageName) {

        V1Pod podBody = (V1Pod)
                deserialize(Template.TerminalPodTemplate, V1Pod.class);

        podBody.getMetadata().setNamespace(namespace);
        podBody.getMetadata().setName(podName);
        podBody.getMetadata().getLabels().clear();
        podBody.getMetadata().getLabels().put("app", podName);
        podBody.getSpec().getContainers().get(0).setImage(imageName);

        return createPod(namespace, podBody);
    }

    /**
     * create cloudware pod
     *
     * @param namespace
     * @param podName
     * @param imageName
     * @return
     */
    public V1Pod createCloudwarePod(String namespace, String podName, String imageName) {

        V1Pod podBody = (V1Pod)
                deserialize(Template.CloudwarePodTemplate, V1Pod.class);

        podBody.getMetadata().setNamespace(namespace);
        podBody.getMetadata().setName(podName);
        podBody.getMetadata().getLabels().clear();
        podBody.getMetadata().getLabels().put("app", podName);
        podBody.getSpec().getContainers().get(0).setImage(imageName);

        return createPod(namespace, podBody);
    }


    /**
     * create cloudware service
     *
     * @param namespace
     * @param deploymentName
     * @return
     */
    public V1Service createCloudwareService(String namespace, String deploymentName) {

        V1Service serviceBody =
                (V1Service) deserialize(Template.CloudwareServiceTemplate, V1Service.class);
        serviceBody.getMetadata().setNamespace(namespace);
        serviceBody.getMetadata().setName(deploymentName);
        serviceBody.getMetadata().getLabels().clear();
        serviceBody.getMetadata().getLabels().put("app", deploymentName);
        serviceBody.getSpec().getSelector().clear();
        serviceBody.getSpec().getSelector().put("app", deploymentName);

        return createService(namespace, serviceBody);
    }

    /**
     * common api for create k8s pod
     *
     * @param namespace
     * @param podBody
     * @return
     */
    private V1Pod createPod(String namespace, V1Pod podBody) {
        V1Pod result = null;
        try {
            result = coreV1Api.createNamespacedPod(namespace, podBody, "false");
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * common api for create k8s service
     *
     * @param namespace
     * @param serviceBody
     * @return
     */
    private V1Service createService(String namespace, V1Service serviceBody) {
        V1Service result = null;
        try {
            result = coreV1Api.createNamespacedService(namespace, serviceBody, "false");
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * common api for delete k8s service
     *
     * @param namespace
     * @param serviceName
     * @return
     */
    public V1Status deleteService(String namespace, String serviceName) {
        V1Status result = null;
        try {
            result = coreV1Api.deleteNamespacedService(serviceName, namespace, "false");
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * common api for delete k8s pod
     *
     * @param namespace
     * @param podName
     * @return
     */
    public V1Status deletePod(String namespace, String podName) {
        V1Status result = null;
        V1DeleteOptions options = new V1DeleteOptions();
        options.setApiVersion("v1");
        options.setKind("DeleteOptions");
        options.setOrphanDependents(false);

        try {
            result = coreV1Api.deleteNamespacedPod(
                    podName,
                    namespace,
                    options,
                    "false",
                    0,
                    false,
                    "Background");
        } catch (Exception e) {
            // Bug exist
            // e.printStackTrace();
        }
        return result;
    }


    public Object deserialize(String jsonStr, Class<?> targetClass) {
        Object obj = (new Gson()).fromJson(jsonStr, targetClass);
        return obj;
    }


//    /**
//     * common api for delete k8s deployment
//     *
//     * @param namespace
//     * @param deploymentName
//     * @return
//     */
//    public V1Status deleteDeployment(String namespace, String deploymentName) {
//        V1Status result = null;
//        V1DeleteOptions options = new V1DeleteOptions();
//        options.setApiVersion("extensions/v1beta1");
//        options.setKind("DeleteOptions");
//        options.setOrphanDependents(false);
//
//        try {
//            result = extensionsV1beta1Api.deleteNamespacedDeployment(
//                    deploymentName,
//                    namespace,
//                    options,
//                    "false",
//                    0,
//                    false,
//                    "Background");
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }


//    /**
//     * common api for create k8s deployment
//     *
//     * @param namespace
//     * @param deploymentBody
//     * @return
//     */
//    private ExtensionsV1beta1Deployment createDeployment(String namespace, ExtensionsV1beta1Deployment deploymentBody) {
//        ExtensionsV1beta1Deployment result = null;
//        try {
//            result = extensionsV1beta1Api.createNamespacedDeployment(namespace, deploymentBody, "false");
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }


//    /**
//     * create terminal deployment
//     *
//     * @param namespace
//     * @param deploymentName
//     * @param imageName
//     * @return
//     */
//    public ExtensionsV1beta1Deployment createTerminalDeployment(String namespace, String deploymentName, String imageName) {
//
//        ExtensionsV1beta1Deployment deploymentBody =
//                (ExtensionsV1beta1Deployment)
//                        deserialize(Template.TerminalDeploymentTemplate, ExtensionsV1beta1Deployment.class);
//        deploymentBody.getMetadata().setName(deploymentName);
//        deploymentBody.getSpec().getSelector().getMatchLabels().clear();
//        deploymentBody.getSpec().getSelector().getMatchLabels().put("app", deploymentName);
//        deploymentBody.getSpec().getTemplate().getMetadata().getLabels().put("app", deploymentName);
//        deploymentBody.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(imageName);
//
//        return createDeployment(namespace, deploymentBody);
//    }
//
//    /**
//     * create cloudware deployment
//     *
//     * @param namespace
//     * @param deploymentName
//     * @param imageName
//     * @return
//     */
//    public ExtensionsV1beta1Deployment createCloudwareDeployment(String namespace, String deploymentName, String imageName) {
//
//        ExtensionsV1beta1Deployment deploymentBody =
//                (ExtensionsV1beta1Deployment)
//                        deserialize(Template.CloudwareDeplymentTemplate, ExtensionsV1beta1Deployment.class);
//        deploymentBody.getMetadata().setName(deploymentName);
//        deploymentBody.getMetadata().setNamespace(namespace);
//        deploymentBody.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(imageName);
//        deploymentBody.getSpec().getSelector().getMatchLabels().clear();
//        deploymentBody.getSpec().getSelector().getMatchLabels().put("app", deploymentName);
//        deploymentBody.getSpec().getTemplate().getMetadata().getLabels().clear();
//        deploymentBody.getSpec().getTemplate().getMetadata().getLabels().put("app", deploymentName);
//
//        return createDeployment(namespace, deploymentBody);
//    }
}