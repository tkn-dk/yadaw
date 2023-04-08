/**
 * Circular buffer
 */
#include <stddef.h>

template <typename T>
class YCircBuf
{
public:
	YCircBuf();
	YCircBuf( size_t size );
	~YCircBuf();

	bool isEmpty()
	{
		return ( rdp == wrp );
	}

	bool isFull()
	{
		unsigned int nextWp = wrp + 1;
		if( nextWp >= bufSize)
			nextWp == 0;
		return ( nextWp == rdp );
	}

	bool write( T element )
	{
		unsigned int nextWp = wrp + 1;
		if( nextWp >= bufSize )
			nextWp = 0;

		if( nextWp != rdp )
		{
			buf[nextWp] = element;
			wrp = nextWp;
			return true;
		}
		return false;
	}

	bool read( T& element)
	{
		if( rdp == wrp )
			return false;

		element = buf[rdp++];
		if( rdp >= bufSize )
			rdp = 0;
		return true;
	}

private:
	size_t bufSize;
	T *buf;
	unsigned int wrp;
	unsigned int rdp;
};

/* Methods implementation: */

template <typename T> YCircBuf<T>::YCircBuf()
	: YCircBuf(256 )
{
}

template <typename T> YCircBuf<T>::YCircBuf( size_t size )
{
	bufSize = size;
	buf = new T[size];
	wrp = 0;
	rdp = 0;
}

template <typename T> YCircBuf<T>::~YCircBuf()
{
	delete[] buf;
	buf = nullptr;
}

