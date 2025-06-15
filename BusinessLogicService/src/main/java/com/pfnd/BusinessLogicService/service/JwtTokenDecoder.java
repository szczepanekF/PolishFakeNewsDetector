package com.pfnd.BusinessLogicService.service;


import io.jsonwebtoken.Claims;

public interface JwtTokenDecoder {
    Claims decode(String token);
}
