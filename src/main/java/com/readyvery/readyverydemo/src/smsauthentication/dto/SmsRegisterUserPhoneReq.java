package com.readyvery.readyverydemo.src.smsauthentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SmsRegisterUserPhoneReq {
	private String phoneNumber;
}
