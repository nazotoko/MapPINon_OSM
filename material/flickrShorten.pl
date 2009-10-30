#!/bin/perl
# public domain (no copyright)
use Math::BigInt;

$base58="123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
foreach(<STDIN>){
$line=$_;
if(m$li:'http://flic.kr/[^/]+/([0-9]+)'$){
$num=Math::BigInt->new($1);$code="";
while($num->bcmp(58)>=0){
$orig=$num->copy();
$div=($num->bdiv(58))->copy();
$orig->bsub($div->bmul(58));
$code=substr($base58,$orig->as_int(),1).$code;
}
$code=substr($base58,$num->as_int(),1).$code;
s|li:'http://flic.kr/[^/]+/[0-9]+'|li:'http://flic.kr/p/$code'|;
}
print $_;
}