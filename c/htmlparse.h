
#ifndef PCH_H
#define PCH_H

// 添加要在此处预编译的标头
extern "C" _declspec(dllexport) void analysisHTMLConstruct(const char* ms, const int length, unsigned int* position);
extern "C" _declspec(dllexport) void aligned_free(void* _Block);
#endif //PCH_H
