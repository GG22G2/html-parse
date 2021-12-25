// pch.cpp: 与预编译标头对应的源文件
#include "htmlparse.h"
#include <intrin.h>
#include<malloc.h>
#include<stdlib.h>
#include <iostream>
#include<time.h>
#include<chrono>
#include <stdio.h>
#include <immintrin.h>
#include <algorithm>

using namespace std;


char* readFileToString(const char* chars);
int testCharCompare();
unsigned long long startAndEnd(unsigned long long emptyMask, unsigned long long& h);
__inline unsigned int  emptyCharMask(__m256i ymm1, __m256i vpshufbMask, __m256i vpshufbIndexn, __m256i vpshufbIndexh, __m256i zero);
__inline void  startAndEnd8Mask(__m256i* position, __m256i& h, uint32_t xMask1, uint32_t xMask2, uint32_t xMask3, uint32_t xMask4, uint32_t xMask5, uint32_t xMask6, uint32_t xMask7, uint32_t xMask8);
__inline void  extractBit(uint32_t* position, uint32_t& positionIndex, unsigned long long xMask, const int index);
__inline __m256i  startAndEnd8Mask(__m256i& h, __m256i emptyMask, __m256i& last_emptyMask256, __m256i& last_nMask);
__inline __m256i bitCount(__m256i v);
__inline unsigned int* whiteSpaceStartAndEndIndex(const char* ms, const int cStart, const int cEnd);
__inline unsigned int whiteSpaceStartAndEndTialIndex(const char* ms, uint32_t* position, uint32_t positionIndex, const int cStart, const int cEnd, bool hasStart);
template<size_t index> __inline void  candidateCharMask(__m256i ymm1, __m256i& alphaMask256, __m256i& constructMask256, __m256i& emptyMask256, __m256i& lessMask256);
bool isWhiteSpace(char what);




bool alphaTable[256];
bool alphaTableInited = false;

void initTable() {
	alphaTable['!'] = true;
	alphaTable['?'] = true;
	alphaTable['/'] = true;
	for (size_t i = 'A'; i <= 'Z'; i++)
	{
		alphaTable[i] = true;
		alphaTable[i + 32] = true;
	}
	alphaTableInited = true;
}

void aligned_free(void* _Block) {

	_aligned_free(_Block);
}

int main() {
	testCharCompare();
	return 1;
}


int testCharCompare() {

	//const char* htmlString = readFileToString("C:\\Users\\h6706\\Desktop\\123.html");
	const char* htmlString = readFileToString("C:\\Users\\h6706\\Desktop\\az0vn-uwu6s.html");
	//const char* htmlString = readFileToString("C:\\Users\\h6706\\Desktop\\321.html");
	//const char* htmlString = "<div> 3 <12 </div><div> <a id = '' hred = \" \" class ></a><div/>    <div>3<12 </div><div> <a id = '' hred = \" \" class ></a><div/>";
	const int ms_length = strlen(htmlString);
	std::cout << ms_length << endl;
	unsigned long long time = 0;
	unsigned long long si = 0;
	for (size_t i = 0; i < 10000; i++)
	{
		auto start = chrono::high_resolution_clock::now();


		unsigned int plength = ms_length / 2;
		unsigned int* position = (unsigned int*)_aligned_malloc(4 * (plength), 32);

		analysisHTMLConstruct(htmlString, ms_length, position);
		//unsigned int* result2 = whiteSpaceStartAndEndIndex(htmlString, 0,ms_length);
		//for (size_t i = 0; i < 149405; i++)
		//{
		//	if (result[i] != result2[i]) {
		//		std::cout << i << endl;
		//	}
		//}

		si += position[0];
		auto end = chrono::high_resolution_clock::now();
		time += chrono::duration_cast<chrono::microseconds>(end - start).count();
		_aligned_free(position);
		//free(result2);
	}
	std::cout << time * 1.0 / 10000 / 1000 << endl;

	std::cout << si << "\n";

	return 0;
}



/*
*
* html结构分析，构建跳转索引
*
* 使用avx2指令 一次处理32字节，
*
* 首先将字符串分为四种类型，   （\r \f \n \t \0 空格） （/ > = " '） （ < ） (大小写字母 ？ ！)   ，其他字符不做处理
*
*	根据第一种类型，将单个空格或多个连续空格两端位置标记为1
*	第二种直接标记为1，
*	三和四结合，用于找<a <! <? 这样的结构 ，并将<对应位置标记为1
*
* 通过_tzcnt_u64提取1的位置，每次提取后使用_blsr_u64对该位置置0，这样就可以构建出索引数组了
*
**/
void analysisHTMLConstruct(const char* ms, const int length, unsigned int* position) {
	if (!alphaTableInited) {
		initTable();
	}
	unsigned long long* bitMask = (unsigned long long*)_aligned_malloc((length / 256 + 1) * 64, 32);
	unsigned int bitMaskIndex = 0;
	const  int length1 = length - 255;

	__m256i hMask = _mm256_setr_epi64x(0, 0, 0, 0);

	__m256i last_nMask = _mm256_setr_epi64x(0, 0, 0, 0);

	__m256i alphaMask256 = _mm256_setr_epi64x(0, 0, 0, 0);
	__m256i constructMask256 = _mm256_setr_epi64x(0, 0, 0, 0);
	__m256i emptyMask256 = _mm256_setr_epi64x(0, 0, 0, 0);
	__m256i lessMask256 = _mm256_setr_epi64x(0, 0, 0, 0);

	//__m256i last_lphaMask256 = _mm256_setr_epi64x(0, 0, 0, 0);
	__m256i last_constructMask256 = _mm256_setr_epi64x(0, 0, 0, 0);
	__m256i last_emptyMask256 = _mm256_setr_epi64x(0, 0, 0, 0);
	__m256i last_lessMask256 = _mm256_setr_epi64x(0, 0, 0, 0);

	int i = 0;
	for (; i < length1; i += 256)
	{
		__m256i ymm1 = _mm256_lddqu_si256((__m256i*) & ms[i]);
		__m256i ymm2 = _mm256_lddqu_si256((__m256i*) & ms[i + 32]);
		__m256i ymm3 = _mm256_lddqu_si256((__m256i*) & ms[i + 64]);
		__m256i ymm4 = _mm256_lddqu_si256((__m256i*) & ms[i + 96]);
		__m256i ymm5 = _mm256_lddqu_si256((__m256i*) & ms[i + 128]);
		__m256i ymm6 = _mm256_lddqu_si256((__m256i*) & ms[i + 160]);
		__m256i ymm7 = _mm256_lddqu_si256((__m256i*) & ms[i + 192]);
		__m256i ymm8 = _mm256_lddqu_si256((__m256i*) & ms[i + 224]);

		candidateCharMask<0>(ymm1, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<1>(ymm2, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<2>(ymm3, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<3>(ymm4, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<4>(ymm5, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<5>(ymm6, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<6>(ymm7, alphaMask256, constructMask256, emptyMask256, lessMask256);
		candidateCharMask<7>(ymm8, alphaMask256, constructMask256, emptyMask256, lessMask256);
		//ymm是上一次循环的结果，并且经过进位判断了，返回ymm用于和last_constructMask256，last_lessMask256 或运算后就可以保存了
		__m256i ymm = startAndEnd8Mask(hMask, emptyMask256, last_emptyMask256, last_nMask);

		//处理 lessMask256 和 alphaMask256  判断有没有小于号后紧跟字母或！或?的
		__m256i alphaLeft = _mm256_srli_epi64(alphaMask256, 1);
		alphaMask256 = _mm256_slli_epi64(alphaMask256, 63);

		//这里访问256可能越界，但是如果引入if分支，性能怕会下降不少. 越界只会存在一次，可以循环结束处理
		unsigned long long z = 0x0L;
		bool c = alphaTable[ms[i + 256]];
		__m256i alpha_hMask = _mm256_setr_epi64x(0, 0, (z | c) << 63, 0);

		alpha_hMask = _mm256_permute2x128_si256(alpha_hMask, alphaMask256, 0x13);
		alpha_hMask = _mm256_alignr_epi8(alpha_hMask, alphaMask256, 8);


		// alpha_hMask 和 alphaLeft运算，用alpha_hMask中的1 替换alphaLeft对应位置
		alphaLeft = _mm256_or_si256(alpha_hMask, alphaLeft);

		lessMask256 = _mm256_and_si256(lessMask256, alphaLeft);


		ymm = _mm256_or_si256(ymm, last_constructMask256);
		ymm = _mm256_or_si256(ymm, last_lessMask256);

		_mm256_store_si256((__m256i*) & bitMask[bitMaskIndex], ymm);
		last_constructMask256 = constructMask256;

		alpha_hMask = alphaMask256;
		last_lessMask256 = lessMask256;

		bitMaskIndex += 4;
	}
	//处理结尾不足256的部分，以及最后一个last_emptyMask256中的内容
	if (i > 0) {
		__m256i   needReturn;
		if (!isWhiteSpace(ms[i - 1]) && isWhiteSpace(ms[i])) {
			needReturn = _mm256_setr_epi64x(1, 0, 0, 0);
		}
		else {
			needReturn = _mm256_setr_epi64x(0, 0, 0, 0);
		}

		// h3和last_nMask 做类似h和h2的操作
		__m256i h4 = _mm256_permute2x128_si256(last_nMask, needReturn, 0x21);
		last_nMask = _mm256_alignr_epi8(h4, last_nMask, 8);

		//h3是1，代表一定是边界，将last_emptyMask256中最高位的值变成h3
		last_nMask = _mm256_slli_epi64(last_nMask, 63);
		last_emptyMask256 = _mm256_or_si256(last_emptyMask256, last_nMask);

		last_emptyMask256 = _mm256_or_si256(last_emptyMask256, last_constructMask256);
		last_emptyMask256 = _mm256_or_si256(last_emptyMask256, last_lessMask256);
	}
	_mm256_store_si256((__m256i*) & bitMask[bitMaskIndex], last_emptyMask256);
	bitMaskIndex += 4;

	//结尾字符处理
	//判断ms[i-1]位置字符是不是空格  是空格时 为true
	bool hasStart = (bool)((_int64*)&hMask)[3];


	unsigned int positionIndex = 1;

	for (int j = 4; j < bitMaskIndex; j++) {
		unsigned long long xMask = bitMask[j];
		unsigned int cnt = __popcnt64(xMask);
		unsigned int next_base = positionIndex + cnt;
		extractBit(position, positionIndex, xMask, (j - 4) * 64);
		positionIndex = next_base;
	}

	positionIndex = whiteSpaceStartAndEndTialIndex(ms, position, positionIndex, i, length, hasStart);
	_aligned_free(bitMask);
	position[0] = positionIndex;
}


__m256i  startAndEnd8Mask(__m256i& h, __m256i emptyMask, __m256i& last_emptyMask256, __m256i& last_nMask) {
	__m256i oneMask = _mm256_set1_epi64x(1);
	__m256i lowMask = _mm256_set1_epi64x(0xfffffffffffffffeL);
	__m256i	highMask = _mm256_set1_epi64x(0x7fffffffffffffffL);

	__m256i rigth = _mm256_andnot_si256(_mm256_slli_epi64(emptyMask, 1), emptyMask);
	__m256i left = _mm256_andnot_si256(_mm256_srli_epi64(emptyMask, 1), emptyMask);

	__m256i h2 = _mm256_srli_epi64(left, 63);	//当前循环需要用的进位，也是给下一个循环用的进位

	h = _mm256_permute2x128_si256(h, h2, 0x21);
	h = _mm256_alignr_epi8(h2, h, 8);

	//处理left左移1次后进位为的bit
	__m256i A = _mm256_and_si256(_mm256_xor_si256(h, rigth), oneMask);
	__m256i B = _mm256_and_si256(lowMask, rigth);

	//判断A中1是边界，还是要移位后才算边界
	__m256i  needReturn = _mm256_and_si256(rigth, A);
	__m256i  needSet1 = _mm256_xor_si256(needReturn, A);

	rigth = _mm256_or_si256(A, B);

	//给上一个循环结果用的进位,处理到这里，如果A中还有1，则代表上一个循环的
	__m256i h3 = needReturn;// _mm256_slli_epi64(A, 63);

	// h3和last_nMask 做类似h和h2的操作
	__m256i h4 = _mm256_permute2x128_si256(last_nMask, h3, 0x21);
	last_nMask = _mm256_alignr_epi8(h4, last_nMask, 8);

	//h3是1，代表一定是边界，将last_emptyMask256中最高位的值变成h3
	last_nMask = _mm256_slli_epi64(last_nMask, 63);
	last_emptyMask256 = _mm256_or_si256(last_emptyMask256, last_nMask);


	__m256i returnMask = last_emptyMask256;

	rigth = _mm256_srli_epi64(rigth, 1);
	rigth = _mm256_or_si256(rigth, needSet1);
	left = _mm256_slli_epi64(left, 1);
	emptyMask = _mm256_or_si256(left, rigth);

	last_emptyMask256 = emptyMask;
	h = h2;
	last_nMask = h3;

	return returnMask;
}

//这里是耗时大头，  大约占50%耗时
__inline void  extractBit(uint32_t* position, uint32_t& positionIndex, unsigned long long xMask, const int index) {
	while (xMask) {
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);
		position[positionIndex++] = index + _tzcnt_u64(xMask);
		xMask = _blsr_u64(xMask);

	}
}


template<size_t index>
__inline void  candidateCharMask(__m256i ymm1, __m256i& alphaMask256, __m256i& constructMask256, __m256i& emptyMask256, __m256i& lessMask256) {
	//低四位的转换
	__m256i vpshufbCharMaskn = _mm256_setr_epi64x(0x1303030303130bc2L, 0x0d21a18101838303L, 0x1303030303130bc2L, 0x0d21a18101838303L);
	//高四位的转换
	__m256i vpshufbCharMaskh = _mm256_setr_epi64x(0x0201020124580080L, 0, 0x0201020124580080L, 0);
	//字符类型判断
	__m256i alphaMask = _mm256_set1_epi8(0b00001111);
	__m256i constructMask = _mm256_set1_epi8(0b00110000);
	__m256i emptyMask = _mm256_set1_epi8(0b11000000);
	//全0
	__m256i zero = _mm256_set1_epi64x(0);
	//每个字节高四位置0
	__m256i vpshufbMask = _mm256_set1_epi64x(0x0f0f0f0f0f0f0f0fL);
	// < 号判断
	__m256i lessSignMask = _mm256_set1_epi8('<');

	__m256i ymm2 = _mm256_srli_epi16(ymm1, 4);
	ymm2 = _mm256_and_si256(ymm2, vpshufbMask);
	__m256i ymm3 = _mm256_shuffle_epi8(vpshufbCharMaskn, ymm1);
	ymm2 = _mm256_shuffle_epi8(vpshufbCharMaskh, ymm2);
	ymm3 = _mm256_and_si256(ymm3, ymm2);
	/**
	*ymm3中包含了三种类型的字符：   结构字符     大小写字母、？、！    以及空字符
	*
	* 通过：
	*	每个字节与上 0000 1111 得到 大小写字母 ？ ！ /
	*	每个字节与上 0011 0000 得到 五种代表结构的字符	 > = " '
	*	每个字节与上 1100 0000 得到 六种空字符		\r \f \n \t \0 空格
	*/
	__m256i  alpha = _mm256_and_si256(ymm3, alphaMask);
	__m256i construct = _mm256_and_si256(ymm3, constructMask);
	__m256i  empty = _mm256_and_si256(ymm3, emptyMask);

	__m256i less = _mm256_cmpeq_epi8(ymm1, lessSignMask);

	//_mm256_cmpgt_epi8 是有符号8字节比较，因此a3要右移再比较
	alpha = _mm256_cmpgt_epi8(alpha, zero);
	construct = _mm256_cmpgt_epi8(construct, zero);

	// a1的字节第8位可能是1，左移右移一次再做比较
	empty = _mm256_srli_epi64(empty, 1);
	empty = _mm256_cmpgt_epi8(empty, zero);

	alphaMask256 = _mm256_insert_epi32(alphaMask256, _mm256_movemask_epi8(alpha), index);
	constructMask256 = _mm256_insert_epi32(constructMask256, _mm256_movemask_epi8(construct), index);
	emptyMask256 = _mm256_insert_epi32(emptyMask256, _mm256_movemask_epi8(empty), index);
	lessMask256 = _mm256_insert_epi32(lessMask256, _mm256_movemask_epi8(less), index);
}

__inline unsigned int  emptyCharMask(__m256i ymm1, __m256i vpshufbMask, __m256i vpshufbIndexn, __m256i vpshufbIndexh, __m256i zero) {
	__m256i ymm2 = _mm256_srli_epi16(ymm1, 4);
	ymm2 = _mm256_and_si256(ymm2, vpshufbMask);
	__m256i ymm3 = _mm256_shuffle_epi8(vpshufbIndexn, ymm1);
	ymm2 = _mm256_shuffle_epi8(vpshufbIndexh, ymm2);
	ymm3 = _mm256_and_si256(ymm3, ymm2);
	ymm3 = _mm256_cmpgt_epi8(ymm3, zero);
	unsigned int  xMask = _mm256_movemask_epi8(ymm3);
	return xMask;
}


//将连续的1序列中间部分置0，高位1左移1位；单个1左移，变成连续两个1
__inline unsigned long long startAndEnd(unsigned long long emptyMask, unsigned long long& h) {
	unsigned long long rigth = emptyMask & ~(emptyMask << 1); //连续1（1代表空格）序列中最左侧的1
	unsigned long long left = emptyMask & ~(emptyMask >> 1);   //连续1（1代表空格）序列中最右侧的1
	unsigned long h2 = left >> 63;
	left = left << 1;
	emptyMask = left | rigth;
	emptyMask = ((h ^ emptyMask) & 0x1) | (emptyMask & 0xfffffffffffffffeL);
	h = h2;
	return emptyMask;
}

char* readFileToString(const char* chars) {
	FILE* fp;
#pragma warning(suppress : 4996)
	fp = fopen(chars, "r");
	fseek(fp, 0, SEEK_END);
	int file_size;
	file_size = ftell(fp);
	char* tmp;
	fseek(fp, 0, SEEK_SET);
	tmp = (char*)malloc(file_size * sizeof(char));
	fread(tmp, file_size, sizeof(char), fp);//个人觉得第三个参数不对
	return tmp;
}

__inline __m256i bitCount(__m256i v) {
	__m256i lookup = _mm256_setr_epi8(0, 1, 1, 2, 1, 2, 2, 3, 1, 2,
		2, 3, 2, 3, 3, 4, 0, 1, 1, 2, 1, 2, 2, 3,
		1, 2, 2, 3, 2, 3, 3, 4);
	__m256i low_mask = _mm256_set1_epi8(0x0f);
	__m256i lo = _mm256_and_si256(v, low_mask);
	__m256i hi = _mm256_and_si256(_mm256_srli_epi32(v, 4), low_mask);
	__m256i popcnt1 = _mm256_shuffle_epi8(lookup, lo);
	__m256i popcnt2 = _mm256_shuffle_epi8(lookup, hi);
	__m256i total = _mm256_add_epi8(popcnt1, popcnt2);
	total = _mm256_sad_epu8(total, _mm256_setzero_si256());
	return total;
}

__inline unsigned int* whiteSpaceStartAndEndIndex(const char* ms, const int cStart, const int cEnd) {
	unsigned int* tempPosition = (unsigned int*)malloc(4 * (cEnd - cStart));
	int continuousWhiteSpace = 0;
	int start = 0;
	int positionIndex = 1;


	for (int j = cStart; j < cEnd; j++) {
		char b = ms[j];
		if (b == '\0' || b == '\t' || b == '\n' || b == '\f' || b == '\r' || b == ' ') {
			if (continuousWhiteSpace == 0) {
				start = j;
			}
			continuousWhiteSpace++;
		}
		else if (continuousWhiteSpace > 0) {
			continuousWhiteSpace = 0;
			if (positionIndex == 0) {
				if (start > 0) {
					tempPosition[positionIndex++] = start - 1;
				}
			}
			else if (tempPosition[positionIndex - 1] != start - 1) {
				tempPosition[positionIndex++] = start - 1;
			}

			tempPosition[positionIndex++] = j;
		}


		if (b == '=' || b == '\"' || b == '\'' || b == '>') {
			if (tempPosition[positionIndex - 1] < j)
				tempPosition[positionIndex++] = j;
		}

		if (b == '<') {
			if (positionIndex == 1 || (tempPosition[positionIndex - 1] < j)) {
				char nextC = ms[j + 1];
				bool f1 = nextC == '!' || nextC == '?' || nextC == '/';
				bool f2 = (nextC >= 'a' && nextC <= 'z') || (nextC >= 'A' && nextC <= 'Z');
				if (f1 || f2) {
					tempPosition[positionIndex++] = j;
				}
			}

		}

	}


	tempPosition[0] = positionIndex;
	return tempPosition;
}

__inline unsigned int whiteSpaceStartAndEndTialIndex(const char* ms, uint32_t* position, uint32_t positionIndex, const int cStart, const int cEnd, bool hasStart) {
	int continuousWhiteSpace = 0;
	int start = 0;

	if (hasStart) {
		continuousWhiteSpace = 1;
	}

	for (int j = cStart; j < cEnd; j++) {
		char b = ms[j];

		//判断连续的空格，
		if (isWhiteSpace(b)) {
			if (continuousWhiteSpace == 0) {
				start = j;
			}
			continuousWhiteSpace++;
		}
		else if (continuousWhiteSpace > 0) {
			continuousWhiteSpace = 0;
			if (hasStart) {
				hasStart = false;
			}
			else {
				// position[0] 用于记录长度，实际的positionIndex 是从1开始的
				if (positionIndex == 1) {
					if (start > 0) {
						position[positionIndex++] = start - 1;
					}
				}
				else if (position[positionIndex - 1] != start - 1) {
					position[positionIndex++] = start - 1;
				}
			}
			position[positionIndex++] = j;
		}

		//判断是不是结构字符
		if (b == '=' || b == '\"' || b == '\'' || b == '>') {
			if (position[positionIndex - 1] < j)
				position[positionIndex++] = j;
		}

		//判断是不是一个标签的开始   后续加上对结束标记的判断
		if (b == '<') {
			if (positionIndex == 1 || (position[positionIndex - 1] < j)) {
				char nextC = ms[j + 1];
				bool f1 = nextC == '!' || nextC == '?' || nextC == '/';
				bool f2 = (nextC >= 'a' && nextC <= 'z') || (nextC >= 'A' && nextC <= 'Z');
				if (f1 || f2) {
					position[positionIndex++] = j;
				}
			}
		}

	}
	return positionIndex;
}


bool isWhiteSpace(char what) {
	if (what == '\0' || what == '\t' || what == '\n' || what == '\f' || what == '\r' || what == ' ') {
		return true;
	}
	return false;
}