package com.canarylogic.focalpoint.utils

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.*;
import java.security.MessageDigest;
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.canarylogic.base.RestException;
import com.canarylogic.base.ExMessages


class EncryptionUtils {
	private static final def DEFAULT_ENCODING = "UTF-8"
	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	public static String encrypToMd5(String plainPassword) {
		byte[] md5hash = MessageDigest.getInstance("MD5").digest(plainPassword.getBytes("UTF-8"));
		StringBuilder hexString = new StringBuilder(md5hash.length * 2);
		for (byte b : md5hash) {
			if ((b & 0xff) < 0x10) hexString.append("0");
			hexString.append(Long.toString(b & 0xff, 16));
		}
		def signature = hexString.toString();
		return signature
	}
	
	public static String calcSignature(def  url,String secretKey) {
		SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(DEFAULT_ENCODING), HMAC_SHA256_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
		mac.init(signingKey)
		byte[] rawHmac = mac.doFinal(url.getBytes())
		return new String(Base64.encodeBase64(rawHmac))
	}
	
	//http://localhost:8080/faceless/base/auth?userId=mark@canary.com&timestamp=1298402016961
	public static String getSignatureUrl(def paramsMap) {
		def baseUrl = ConfigurationHolder.config.grails.serverURL
		if(!baseUrl) throw new RestException(ExMessages.AUTHENCIATION_FAILED,"Signature Url not found in getSignatureUrl()")
		def signatureUrl = "${baseUrl}/${paramsMap.controller}/${paramsMap.action}?userId=${paramsMap.userId}&timestamp=${paramsMap.timestamp}"
		return signatureUrl
	}
	
	public static  boolean checkMandatoryParams(def paramsMap) {
		if(paramsMap.applicationId == null || paramsMap.service == null || paramsMap.userId== null || paramsMap.signature==null
			 || paramsMap.timestamp == null)
			throw new RestException(ExMessages.AUTHENCIATION_FAILED,"Mandatory fields are missing")
		
	}
	
	public static boolean validateSignature(def paramsMap, String secretKey){
		def signatureUrl = getSignatureUrl(paramsMap)
		println "SIGNATURE URL is $signatureUrl"
		def calculatedSignature =  calcSignature(signatureUrl,secretKey)
		def usersSig = paramsMap.signature
		def repUserSig = usersSig.replaceAll("%2B","+")
//		 log.debug "Users signature is ${paramsMap.signature} and calculate one is $calculatedSignature"
		if(repUserSig != calculatedSignature) {
			println """FAILED !!! $paramsMap.userId signature is $repUserSig
			and Calculated Signature is $calculatedSignature, systems url is $signatureUrl"""
			throw new RestException(ExMessages.AUTHENCIATION_FAILED, "Signature mismatch $repUserSig (userSign) does not match $calculatedSignature")
		}
	}
			
	
}
