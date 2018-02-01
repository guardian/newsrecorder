import React from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';

class RecordButton extends React.Component {
    static propTypes = {
        episodeId: PropTypes.string.isRequired,
        channelId: PropTypes.string.isRequired,
        setForRecording: PropTypes.bool.isRequired,
        errorCallback: PropTypes.func,
        successCallback: PropTypes.func
    };

    constructor(props){
        super(props);

        this.recordClicked = this.recordClicked.bind(this);
    }

    recordClicked(){
        axios.put("/api/programme/" + this.props.channelId + "/" + this.props.episodeId + "/record").then(response=>{
            if(this.props.successCallback) this.props.successCallback("record",this.props.channelId, this.props.episodeId);
        }).catch(error=>{
            if(this.props.errorCallback) this.props.errorCallback(error);
            console.error(error);
        })
    }

    cancelClicked(){
        axios.delete("/api/programme/" + this.props.channelId + "/" + this.props.episodeId + "/record").then(response=>{
            if(this.props.successCallback) this.props.successCallback("cancel",this.props.channelId, this.props.episodeId);
        }).catch(error=>{
            if(this.props.errorCallback) this.props.errorCallback(error);
            console.error(error);
        })
    }

    render(){
        if(this.props.setForRecording){
            return <i className="fa fa-stop" onClick={this.cancelClicked}/>
        } else {
            return <i className="fa fa-crosshairs" onClick={this.recordClicked}/>
        }
    }
}

export default RecordButton;