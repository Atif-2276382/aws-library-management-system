import {useEffect,useState}
from "react";

import {

    getMembers,
    createMember,
    updateMember,
    deleteMember

}
from "../services/memberService";

import {
    getUsers
}
from "../services/userService";

function Members(){

    const [members,setMembers]
        = useState([]);

    const [users,setUsers]
        = useState([]);

    const [editingId,setEditingId]
        = useState(null);

    const [form,setForm]
        = useState({

            name:"",
            membershipId:"",
            userId:""
        });

    useEffect(()=>{

        loadMembers();

        loadUsers();

    },[]);

    const loadMembers=
        async()=>{

        try{

            const response=
                await getMembers();

            setMembers(
                response.data
            );

        }catch(error){

            console.error(
                error
            );
        }
    };

    const loadUsers=
        async()=>{

        try{

            const response=
                await getUsers();

            setUsers(
                response.data
            );

        }catch(error){

            console.error(
                error
            );
        }
    };

    const handleChange=
        (e)=>{

        setForm({

            ...form,

            [e.target.name]:
                e.target.value
        });
    };

    const handleSubmit=
        async(e)=>{

        e.preventDefault();

        try{

            const payload={

                name:
                    form.name,

                membershipId:
                    form.membershipId,

                userId:
                    Number(
                        form.userId
                    )
            };

            if(editingId){

                await updateMember(

                    editingId,

                    payload
                );

            }else{

                await createMember(
                    payload
                );
            }

            resetForm();

            loadMembers();

        }catch(error){

            console.error(
                error
            );
        }
    };

    const handleEdit=
        (member)=>{

        setEditingId(
            member.id
        );

        setForm({

            name:
                member.name,

            membershipId:
                member.membershipId,

            userId:
                member.user?.id
        });
    };

    const handleDelete=
        async(id)=>{

        try{

            await deleteMember(
                id
            );

            loadMembers();

        }catch(error){

            console.error(
                error
            );
        }
    };

    const resetForm=()=>{

        setEditingId(
            null
        );

        setForm({

            name:"",
            membershipId:"",
            userId:""
        });
    };

    return(

        <div
            className=
            "container mt-4"
        >

            <h2>

                Member Management

            </h2>

            <form
                onSubmit={
                    handleSubmit
                }

                className=
                "mb-4"
            >

                <input

                    type="text"

                    name="name"

                    placeholder=
                    "Member Name"

                    className=
                    "form-control mb-2"

                    value={
                        form.name
                    }

                    onChange={
                        handleChange
                    }
                />

                <input

                    type="text"

                    name=
                    "membershipId"

                    placeholder=
                    "Membership Id"

                    className=
                    "form-control mb-2"

                    value={
                        form.membershipId
                    }

                    onChange={
                        handleChange
                    }
                />

                <select

                    name="userId"

                    className=
                    "form-control mb-3"

                    value={
                        form.userId
                    }

                    onChange={
                        handleChange
                    }
                >

                    <option
                        value=""
                    >

                        Select User

                    </option>

                    {

                        users.map(
                            user=>(

                                <option

                                    key={
                                        user.id
                                    }

                                    value={
                                        user.id
                                    }
                                >

                                    {
                                        user.username
                                    }

                                </option>
                            )
                        )
                    }

                </select>

                <button

                    className=
                    "btn btn-primary"
                >

                    {
                        editingId

                        ? "Update Member"

                        : "Add Member"
                    }

                </button>

            </form>

            <table

                className=
                "table table-bordered"
            >

                <thead>

                <tr>

                    <th>ID</th>

                    <th>Name</th>

                    <th>Membership ID</th>

                    <th>User</th>

                    <th>Actions</th>

                </tr>

                </thead>

                <tbody>

                {

                    members.map(
                        member=>(

                            <tr
                                key={
                                    member.id
                                }
                            >

                                <td>

                                    {
                                        member.id
                                    }

                                </td>

                                <td>

                                    {
                                        member.name
                                    }

                                </td>

                                <td>

                                    {
                                        member.membershipId
                                    }

                                </td>

                                <td>

                                    {
                                        member.user
                                        ?.username
                                    }

                                </td>

                                <td>

                                    <button

                                        className=
                                        "btn btn-warning btn-sm me-2"

                                        onClick=
                                        {()=>

                                            handleEdit(
                                                member
                                            )
                                        }
                                    >

                                        Edit

                                    </button>

                                    <button

                                        className=
                                        "btn btn-danger btn-sm"

                                        onClick=
                                        {()=>

                                            handleDelete(
                                                member.id
                                            )
                                        }
                                    >

                                        Delete

                                    </button>

                                </td>

                            </tr>
                        )
                    )
                }

                </tbody>

            </table>

        </div>
    );
}

export default Members;