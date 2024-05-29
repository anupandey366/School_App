package com.application.myschoolapp.utils.agoraTokenUtils;

interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}