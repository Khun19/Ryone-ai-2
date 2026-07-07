import React, { createContext, useContext, useState, useMemo } from 'react';

// Define the voice assistant states
export const VoiceStatuses = {
  IDLE: 'Idle',
  LISTENING: 'Listening',
  PROCESSING: 'Processing',
  SPEAKING: 'Speaking',
};

const VoiceStateContext = createContext(null);

/**
 * VoiceStateProvider wraps the application to provide a central state machine
 * for tracking assistant statuses: Idle, Listening, Processing, and Speaking.
 */
export const VoiceStateProvider = ({ children }) => {
  const [status, setStatus] = useState(VoiceStatuses.IDLE);

  const value = useMemo(() => {
    const startListening = () => {
      setStatus(VoiceStatuses.LISTENING);
    };

    const detectVoiceCommand = () => {
      // Automatically transition from 'Listening' to 'Processing' upon voice command detection
      setStatus(VoiceStatuses.PROCESSING);
    };

    const generateAIResponse = () => {
      // Automatically transition from 'Processing' to 'Speaking' once the AI response is generated
      setStatus(VoiceStatuses.SPEAKING);
    };

    const finishSpeaking = () => {
      setStatus(VoiceStatuses.IDLE);
    };

    const processVoiceCommand = async (mockDelayMs = 1500) => {
      startListening();
      // Simulate user speaking and end of speech detection
      await new Promise(resolve => setTimeout(resolve, mockDelayMs));
      
      // Voice command detected -> transitions to Processing
      detectVoiceCommand();
      
      // Simulate AI thinking & response generation
      await new Promise(resolve => setTimeout(resolve, mockDelayMs));
      
      // AI response generated -> transitions to Speaking
      generateAIResponse();
      
      // Simulate TTS playback
      await new Promise(resolve => setTimeout(resolve, mockDelayMs * 1.5));
      
      finishSpeaking();
    };

    return {
      status,
      setStatus,
      isIdle: status === VoiceStatuses.IDLE,
      isListening: status === VoiceStatuses.LISTENING,
      isProcessing: status === VoiceStatuses.PROCESSING,
      isSpeaking: status === VoiceStatuses.SPEAKING,
      
      // State transition helpers
      transitionToIdle: finishSpeaking,
      transitionToListening: startListening,
      transitionToProcessing: detectVoiceCommand,
      transitionToSpeaking: generateAIResponse,
      
      // Automated voice processing functions
      startListening,
      detectVoiceCommand,
      generateAIResponse,
      finishSpeaking,
      processVoiceCommand,
    };
  }, [status]);

  return (
    <VoiceStateContext.Provider value={value}>
      {children}
    </VoiceStateContext.Provider>
  );
};

/**
 * useVoiceState custom hook to access the assistant's voice state context.
 */
export const useVoiceState = () => {
  const context = useContext(VoiceStateContext);
  if (!context) {
    throw new Error('useVoiceState must be used within a VoiceStateProvider');
  }
  return context;
};
