import apiClient from "../api/apiClient";

export const login = async(data)=>{

    return apiClient.post(
        "/auth/login",
        data
    );
};

export const register = async(data)=>{

    return apiClient.post(
        "/auth/register",
        data
    );
};