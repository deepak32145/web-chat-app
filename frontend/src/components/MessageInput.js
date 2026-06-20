import React, { useState, useRef } from 'react';
import { FaPaperPlane, FaPlus, FaTimes, FaFile } from 'react-icons/fa';
import { uploadFile } from '../services/api';
import '../styles/MessageInput.css';

const MessageInput = ({ onSendMessage }) => {
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [preview, setPreview] = useState(null); // { type: 'image'|'file', url?, name }
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setSelectedFile(file);

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (ev) => setPreview({ type: 'image', url: ev.target.result, name: file.name });
      reader.readAsDataURL(file);
    } else {
      setPreview({ type: 'file', name: file.name });
    }
  };

  const clearFile = () => {
    setSelectedFile(null);
    setPreview(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!message.trim() && !selectedFile) return;

    setLoading(true);
    try {
      let mediaUrl = null;
      let mediaType = null;

      if (selectedFile) {
        const formData = new FormData();
        formData.append('file', selectedFile);
        const res = await uploadFile(formData);
        mediaUrl = `${process.env.REACT_APP_BASE_URL || 'http://localhost:8080'}${res.data.url}`;
        mediaType = res.data.mediaType;
      }

      await onSendMessage(message, mediaUrl, mediaType);
      setMessage('');
      clearFile();
    } catch (err) {
      console.error('Failed to send:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="message-input-wrapper" onSubmit={handleSubmit}>
      {preview && (
        <div className="file-preview">
          {preview.type === 'image' ? (
            <img src={preview.url} alt="preview" className="preview-img" />
          ) : (
            <div className="preview-file">
              <FaFile />
              <span>{preview.name}</span>
            </div>
          )}
          <button type="button" className="clear-file-btn" onClick={clearFile}>
            <FaTimes />
          </button>
        </div>
      )}

      <div className="input-row">
        <button
          type="button"
          className="attach-btn"
          onClick={() => fileInputRef.current?.click()}
          disabled={loading}
          title="Attach file or image"
        >
          <FaPlus />
        </button>

        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip,.rar,.7z"
          style={{ display: 'none' }}
        />

        <input
          type="text"
          placeholder="Type a message..."
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          disabled={loading}
          className="text-input"
        />

        <button
          type="submit"
          disabled={loading || (!message.trim() && !selectedFile)}
          className="send-btn"
        >
          <FaPaperPlane />
        </button>
      </div>
    </form>
  );
};

export default MessageInput;
