// ISecurityCenter.aidl
package com.wzc.chapter_2_common_lib.binderpool;

// Declare any non-default types here with import statements
// 提供加解密功能
interface ISecurityCenter {
   String encrypt(String content);
   String decrypt(String password);
}
