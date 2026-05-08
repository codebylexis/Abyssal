package core;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.AL11;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

/**
 * Loads an OGG/Vorbis audio file with STB and streams it into an OpenAL
 * source/buffer pair. Supports looping playback. Call play(), stop(), and
 * delete() to manage the source lifecycle.
 */
public class Sound {

	private int bufferId;
	private int sourceId;
	private String filepath;
	
	private boolean isPlaying = false;
	
	public Sound(String filepath, boolean loops) {
		this.filepath = filepath;
		
		// allocate space to store the return info from stb
		MemoryStack.stackPush();
		IntBuffer channelsBuffer = MemoryStack.stackMallocInt(1);
		MemoryStack.stackPush();
		IntBuffer sampleRateBuffer = MemoryStack.stackMallocInt(1);
		
		ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(filepath, channelsBuffer, sampleRateBuffer);
		if(rawAudioBuffer == null) {
			System.out.println("could not load sound " + filepath);
			MemoryStack.stackPop();
			MemoryStack.stackPop();
			return;
		}
		
		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		
		MemoryStack.stackPop();
		MemoryStack.stackPop();
		
		int format = -1;
		if(channels == 1) {
			format = AL11.AL_FORMAT_MONO16;
		}else if(channels == 2) {
			format = AL11.AL_FORMAT_STEREO16;
		}
		
		bufferId = AL11.alGenBuffers();
		AL11.alBufferData(bufferId, format, rawAudioBuffer, sampleRate);
		
		sourceId = AL11.alGenSources();
		
		AL11.alSourcei(sourceId, AL11.AL_BUFFER, bufferId);
		AL11.alSourcei(sourceId, AL11.AL_LOOPING, loops ? 1 : 0);
		AL11.alSourcei(sourceId, AL11.AL_POSITION, 0);
		AL11.alSourcef(sourceId, AL11.AL_GAIN, 0.3f);
		
		LibCStdlib.free(rawAudioBuffer);
	}
	
	public void delete() {
		AL11.alDeleteSources(sourceId);
		AL11.alDeleteBuffers(bufferId);
	}
	
	public void play() {
		int state = AL11.alGetSourcei(sourceId, AL11.AL_SOURCE_STATE);
		if(state == AL11.AL_STOPPED) {
			isPlaying = false;
			AL11.alSourcei(sourceId, AL11.AL_POSITION, 0);
		}
		
		if(!isPlaying) {
			AL11.alSourcePlay(sourceId);
			isPlaying = true;
		}
	}
	
	public void stop() {
		if(isPlaying) {
			AL11.alSourceStop(sourceId);
			isPlaying = false;
		}
	}
	
	public String getFilePath() {
		return filepath;
	}
	
	public boolean isPlaying() {
		int state = AL11.alGetSourcei(sourceId, AL11.AL_SOURCE_STATE);
		if(state == AL11.AL_STOPPED) {
			isPlaying = false;
		}
		
		return isPlaying;
	}
}
