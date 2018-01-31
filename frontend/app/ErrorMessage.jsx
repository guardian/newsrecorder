import React from 'react';
import PropTypes from 'prop-types';

class ErrorMessage extends React.Component {
    static propTypes = {
        axiosError: PropTypes.object.isRequired
    };

    static processErrorObject(error){
        console.log(error.config);
        // Error
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx
            // console.log(error.response.data);
            // console.log(error.response.status);
            // console.log(error.response.headers);
            console.error(error.response.data.error);
            console.error(error.response.data.stackTrace);
            return error.response.data.status + ": " + error.response.data.error;
        } else if (error.request) {
            // The request was made but no response was received
            // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
            // http.ClientRequest in node.js
            console.error(error.request);
            return "No response received from server. See browser console for more information";
        } else {
            // Something happened in setting up the request that triggered an Error
            console.log('Error', error.message);
            return "Unable to set up request";
        }
    }

    render(){
        if(this.props.axiosError) {
            return <p className="error">{ErrorMessage.processErrorObject(this.props.axiosError)}</p>
        } else {
            return <p/>
        }
    }
}

export default ErrorMessage;