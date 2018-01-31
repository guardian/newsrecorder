import React from 'react';
import SortableTable from 'react-sortable-table';
import moment from 'moment';
import PropTypes from 'prop-types';
import FilterButton from './FilterButton.jsx';
import ChannelComponent from './ChannelComponent.jsx';

class DataTable extends React.Component {
    static propTypes = {
        dateFormat: PropTypes.string,
        inputData: PropTypes.object.isRequired,
        channelFilterDeactivated: PropTypes.func.isRequired,
        channelsData: PropTypes.object.isRequired,
        hasChannelFilter: PropTypes.bool
    };

    constructor(props){
        super(props);
    }

    render(){
        const dateFormat = this.props.dateFormat ? this.props.dateFormat : "MMMM Do YYYY, h:mm:ss a";

        const columns = [
            {
                header: "Channel",
                headerProps: {className: "dashboardheader"},
                key: "channelId",
                render: (channelId)=><span>
                    <FilterButton fieldName="channelId" values={channelId} type="plus"
                                 onActivate={this.props.channelFilterActivated}
                                 onDeactivate={this.props.channelFilterDeactivated}
                                 isActive={this.props.hasChannelFilter}
                    />
                    <ChannelComponent channelData={this.props.channelsData} selectedChannel={channelId}/>
                </span>
            },
            {
                header: "Starting time",
                headerProps: {className: "dashboardheader"},
                key: "startTime",
                render: (date)=>moment(date).format(dateFormat)
            },
            {
                header: "Ending time ",
                headerProps: {className: "dashboardheader"},
                key: "endTime",
                render: (date)=>moment(date).format(dateFormat)
            },
            {
                header: "Title",
                headerProps: {className: "dashboardheader"},
                key: "title",
            },
            {
                header: "Description",
                headerProps: {className: "dashboardheader"},
                key: "description",
                render: (desc)=><p className="programme-description">{desc}</p>
            }
        ];

        const style = {
            borderCollapse: "collapse"
        };

        const iconStyle = {
            color: '#aaa',
            paddingLeft: '5px',
            paddingRight: '5px'
        };

        return <SortableTable data={this.props.inputData} columns={columns} style={style} iconStyle={iconStyle}/>
    }
}

export default DataTable;