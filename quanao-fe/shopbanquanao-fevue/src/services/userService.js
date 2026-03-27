// src/services/userService.js

import api from './api'; // Giả định bạn có một instance axios đã cấu hình

const API_URL = '/api/admin/users';

const getUsers = (page = 0, size = 10) => {
    // API GET /api/admin/users?page=0&size=10
    return api.get(API_URL, {
        params: { page, size }
    });
};

const updateUserRole = (userId, newRole) => {
    // API PUT /api/admin/users/{userId}/role?role=ADMIN
    return api.put(`${API_URL}/${userId}/role`, null, {
        params: { role: newRole }
    });
};

export default {
    getUsers,
    updateUserRole,
};