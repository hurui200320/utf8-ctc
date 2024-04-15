# utf8-ctc
Chinese telegraph code, but use UTF8 as a fallback.

[简体中文说明](./readme_zh-CN.md)

## What is CTC? And why use it in 20XX?

CTC is the shorthand for "Chinese Telegraph Code".
It compiles most of the common characters and assigns them a number.
For example, character `汉` is numbered `3352`.
This is a more compact way to index Chinese:
It only uses two bytes (actually it's only 0000 to 9999),
while UTF-8 uses three bytes on average.

While UTF-8 is generally the best option to handle all kinds of characters,
when talking about encoding along with a limitation on length,
CTC becomes useful.

## What is this program for?

This program is written for encoding text on postcards, while we don't want to
deal with encrypted data represented in base58, but still want some level of privacy.
So this program encodes the Chinese into CTC, and if there is a missing character,
it uses either raw UTF-8 or compressed UTF-8 to encode.

> Handwriting and typing numbers are much easier than base64/58.

The design is: when writing postcards, I encode the text either from cli options,
or files, or from `stdin`, then it spills out a bunch of numbers, and I just need
to write them on the postcard.

When someone got the postcard, he can type in the numbers and the program will
print out the decoded message.

## Usage

The program is built using jlink with zulu 21 jdk, so technically you don't need
a Java runtime installed on your pc.
Download the correct build for your usage and invoke `./ctc`, everything should be fine.

> There is no package published to use this codec as a dependency.
> I didn't write to dependency, I didn't test, so I don't want to people use it
> and accidentally cause some disaster.
> You can try with the jitpack, but I won't ensure the result.
> 
> ***If this demand is popular, create an issue and I will work on it to publish as a dependency***

### Encoding

You can encode text from the cli option, file, and stdin.

```bash
./ctc encode -t "天匠染青红，花腰呈袅娜。"
./ctc encode -f message.txt
echo "天匠染青红，花腰呈袅娜。" | ./ctc encode -c UTF-8
```

The `-t <text>` option will omit the `-f <file>` if they are both specified.
When using the stdin, you need to manually tell the program you're done by inputting
`EOF` on a new line.

You can change the `EOF` to something else using `--eof=EOT`, now you can use `EOT`
instead of `EOF`.

You can also change how many codes are printed in one line.
But do be aware this is limited by your terminal width, if it's longer than the width, it will wrap.
By default, it's fit your terminal.
You can change it by option `-w 6`, now you have six codes per line.

When using stdin input, there is some issue with the charset on Windows.
By default, the program uses UTF-8 to read and write everything.
But on windows, it will use `GBK` if the default locale is `zh-CN`;
or use `BIG5` if the default locale is `zh-TW`.
If you don't want this feature, you can use `-c UTF-8` to force the program to
use UTC-8 charset.

Do verify the program output before you write numbers to your postcard.
Sometimes the charset issue can mess you up if you don't check the decoded result.

### Decoding

It's basically the same, but much simpler.
When decoding, the program will print everything using UTF-8,
which most terminals are ok, even on windows.

For decoding, there are only three options: `-t`, `-f` and `--eof`.

You can type any character, and the program will ignore them and only take the digits.
So don't worry if you accidentally typed a letter or something.
Keep typing numbers and you're good.

```bash
./ctc decode -t "1131, 0561, 2676, 7230, 4767, 9976, 5363, 5212, 0701, 5934, 1226, 9975"
```

### Encryption

Starting from v0.1.0, I added a Transposition Cipher using VMPC random generator.
It supports symmetric encryption (offers only the `-k string` option) and 
asymmetric encryption (offering both `-k string` and `--dh hex`) to use X25519 ECDH.

When using symmetric encryption, the key from `-k key` will be encoded using UTF-8
and apply SH3-256.
The result hash will be used as the seed of VMPC generator.

When using asymmetric encryption, the key will be encoded by UTF-8 and apply SHA3-256.
Then use is as our private key to perform X25519 ECDH, the result will be hashed by
SHA3-256, and the hash will be used as the encryption key.

Use `./ctc pubkey key` to calculate the public key when using `-k key`. 

The encryption is based on Transposition Cipher.
The implementation is `#randomize` and `#derandomize` in [Utils.kt](./src/main/kotlin/info/skyblond/ctc/Utils.kt) 

One time of randomization is not safe.
So you can use `-n` to set the iteration amount.
By default, it's 1.2 million times.

## Internal design

### Preprocess

There is some internal processing beyond simply lookup the table and do the substitution.
The CTC is scraped from https://en.wiktionary.org/wiki/Appendix:Chinese_telegraph_code/Mainland_1983.

Before starting encoding characters, we need to replace some text for shorter codes:
+ new lines (`\n`) are replaced by `〷` (code 9999)
+ text like `10日` will be replaced by the char `㏩` (code 9910)

This will save some code and will be transparent for user.
When decoding, reversed processing will be applied to make sure the decoded
text is looking normal.

### UTF-8 fallback

When there is an unknown character, it will save to the buffer and encode them in batches.
For example, when encoding `大家好，my name is XXX。`, the unknown buffer will be `my name is XXX`,
then it will be encoded to utf-8 bytes.
The encoded bytes will be compressed by `java.util.zip.Deflater` with max level (9),
and the result will be kept if the compress result is shorter than the raw form.

For raw UTF-8 bytes, it will be enclosed with `9992` and `9993`, the content
will be representing in 0000 to 9991, like base9992 but all in numbers.

For compressed UTF-8 bytes, it will be enclosed with `9994` and `9995`.
The content representation is the same.

## Contributes

Welcome, but I can't guarantee if I accept the pr or not.
Open issues as long as you have questions, but I can't guarantee to answer them.

TL;DR: I give no guarantee on the program, unless you pay me.
