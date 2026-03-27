// src/services/fileUploadService.js
import apiClient from '@/services/api.js';

const uploadFiles = async (files) => {
  // QUAN TRỌNG NHẤT: Chuyển FileList / raw FileList thành mảng thật
  const fileArray = files.raw ? Array.from(files.raw) : Array.from(files);

  if (fileArray.length === 0) {
    return { data: { urls: [] } }; // không có file → trả mảng rỗng
  }

  const formData = new FormData();

  // Append từng file với key "files" → khớp với @RequestParam("files") ở backend
  fileArray.forEach(file => {
    formData.append('files', file);
  });

  return await apiClient.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

export default {
  uploadFiles
};