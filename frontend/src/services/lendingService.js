import apiClient from "../api/apiClient";

export const issueBook=(data)=>{

    return apiClient.post(
        "/lendings/issue",
        data
    );
};

export const returnBook=(id)=>{

    return apiClient.put(
        `/lendings/return/${id}`
    );
};