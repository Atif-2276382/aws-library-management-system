import apiClient from "../api/apiClient";

export const getAuthors = ()=>{

    return apiClient.get(
        "/authors"
    );
};