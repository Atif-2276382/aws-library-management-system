import apiClient from "../api/apiClient";

export const getUsers = ()=>{

    return apiClient.get(
        "/auth/users"
    );
};