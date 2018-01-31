import React from 'react';
import PropTypes from 'prop-types';

class ChannelComponent extends React.Component {
    static propTypes = {
        channelData: PropTypes.object.isRequired,
        selectedChannel: PropTypes.string.isRequired
    };

    constructor(props){
        super(props);
    }

    render(){
        const myChannel = this.props.channelData[this.props.selectedChannel];

        return <span>
            <img src={myChannel.iconUrl} className="channel-logo"/>{myChannel.displayName}
        </span>
    }
}

export default ChannelComponent;