import apiClient from "../api/apiClient";

export const getNotifications=()=>{

    return apiClient.get(
        "/notifications"
    );
};